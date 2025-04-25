package com.bajaj.webhookclient.config;

import com.bajaj.webhookclient.service.WebhookService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {

    private final WebhookService webhookService;

    public AppStartupRunner(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Override
    public void run(ApplicationArguments args) {
        webhookService.processWebhook();
    }
}