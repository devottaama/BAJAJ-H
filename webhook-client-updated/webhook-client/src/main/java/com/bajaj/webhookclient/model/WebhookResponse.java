package com.bajaj.webhookclient.model;

import java.util.List;

public class WebhookResponse {
    private String webhook;
    private String accessToken;
    private Data data;

    public static class Data {
        private List<User> users;
        private int n;
        private int findId;

        public List<User> getUsers() { return users; }
        public void setUsers(List<User> users) { this.users = users; }

        public int getN() { return n; }
        public void setN(int n) { this.n = n; }

        public int getFindId() { return findId; }
        public void setFindId(int findId) { this.findId = findId; }
    }

    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}