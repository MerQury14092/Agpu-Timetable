package com.merqury.agpu.notificatoin.service;

import com.merqury.agpu.notificatoin.DTO.Webhook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WebhookRegistryService {
    private final TimetableChangesPublisher changesPublisher;
    private final List<Webhook> memory;
    private static final WebhookRegistryService singleton;

    static {
        singleton = new WebhookRegistryService();
    }

    public static WebhookRegistryService singleton(){
        return singleton;
    }

    private WebhookRegistryService() {
        this.memory = new ArrayList<>();
        this.changesPublisher = TimetableChangesPublisher.singleton();
    }

    /**
     * @return status code
     */
    public int addWebhook(Webhook webhook){
        if(hasWebhook(webhook))
            return 1;
        changesPublisher.addSubscriber(webhook);
        memory.add(webhook);
        return 0;
    }

    public List<Webhook> getWebhooks(String url){
        return memory.stream()
                .filter(
                        el -> Objects.equals(el.getUrl(), url)
                )
                .toList();
    }

    public boolean hasWebhook(Webhook webhook){
        for(Webhook currentWebhook: getWebhooks(webhook.getUrl()))
            if(Objects.equals(currentWebhook.getGroup(), webhook.getGroup()))
                return true;
        return false;
    }

    public void removeWebhook(String url){
        removeSubscribesByUrl(url);
        removeFromMemoryByUrl(url);
    }

    private void removeSubscribesByUrl(String url){
        memory.stream()
                .filter(element -> Objects.equals(element.getUrl(), url))
                .forEach(changesPublisher::removeSubscriber);
    }

    private void removeFromMemoryByUrl(String url){
        memory.removeIf(element -> Objects.equals(element.getUrl(), url));
    }
}
