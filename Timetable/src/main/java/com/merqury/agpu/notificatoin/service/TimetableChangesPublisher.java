package com.merqury.agpu.notificatoin.service;

import com.merqury.agpu.DTO.TimetableDay;
import com.merqury.agpu.notificatoin.interfaces.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class TimetableChangesPublisher {
    private final List<Subscriber> subscribers;
    private static final TimetableChangesPublisher singleton;

    static {
        singleton = new TimetableChangesPublisher();
    }

    private TimetableChangesPublisher(){
        subscribers = new ArrayList<>();
    }

    public static TimetableChangesPublisher singleton(){
        return singleton;
    }

    public void addSubscriber(Subscriber subscriber){
        subscribers.add(subscriber);
    }

    public void removeSubscriber(Subscriber subscriber){
        subscribers.remove(subscriber);
    }

    public void publishNotification(String id, TimetableDay changes){
        for(Subscriber subscriber: subscribers)
            subscriber.handleNotification(id, changes);
    }
}
