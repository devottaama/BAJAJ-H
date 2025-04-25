package com.bajaj.webhookclient.service;

import com.bajaj.webhookclient.model.User;
import com.bajaj.webhookclient.model.WebhookResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void processWebhook() {
        String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "REG12347");
        requestBody.put("email", "john@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(generateWebhookUrl, entity, WebhookResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            WebhookResponse webhookResponse = response.getBody();
            List<List<Integer>> outcome;

            int regSuffix = Integer.parseInt("12347".replaceAll("\D", ""));
            int lastTwoDigits = regSuffix % 100;

            if (lastTwoDigits % 2 == 0) {
                outcome = computeNthLevel(webhookResponse.getData().getUsers(), webhookResponse.getData().getFindId(), webhookResponse.getData().getN());
            } else {
                outcome = computeMutualFollowers(webhookResponse.getData().getUsers());
            }

            sendResult(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), outcome);
        }
    }

    private List<List<Integer>> computeMutualFollowers(List<User> users) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, Set<Integer>> userFollowsMap = new HashMap<>();

        for (User user : users) {
            userFollowsMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }

        Set<String> seen = new HashSet<>();

        for (User user : users) {
            for (Integer followeeId : user.getFollows()) {
                if (userFollowsMap.containsKey(followeeId) && userFollowsMap.get(followeeId).contains(user.getId())) {
                    int minId = Math.min(user.getId(), followeeId);
                    int maxId = Math.max(user.getId(), followeeId);
                    String pairKey = minId + ":" + maxId;
                    if (!seen.contains(pairKey)) {
                        result.add(Arrays.asList(minId, maxId));
                        seen.add(pairKey);
                    }
                }
            }
        }

        return result;
    }

    private List<List<Integer>> computeNthLevel(List<User> users, int findId, int n) {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (User user : users) {
            graph.put(user.getId(), user.getFollows());
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(findId);
        visited.add(findId);

        for (int level = 0; level < n; level++) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Integer current = queue.poll();
                for (Integer neighbor : graph.getOrDefault(current, new ArrayList<>())) {
                    if (!visited.contains(neighbor)) {
                        queue.offer(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }

        List<Integer> result = new ArrayList<>(queue);
        Collections.sort(result);
        return Collections.singletonList(result);
    }

    private void sendResult(String webhookUrl, String accessToken, List<List<Integer>> outcome) {
        Map<String, Object> resultBody = new HashMap<>();
        resultBody.put("regNo", "REG12347");
        resultBody.put("outcome", outcome);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(resultBody, headers);

        int attempts = 0;
        while (attempts < 4) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    break;
                }
            } catch (Exception e) {
                attempts++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}