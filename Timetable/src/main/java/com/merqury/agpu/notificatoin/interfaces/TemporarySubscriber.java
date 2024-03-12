package com.merqury.agpu.notificatoin.interfaces;


import com.merqury.agpu.DTO.TimetableDay;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.merqury.agpu.general.Foundation.*;

public class TemporarySubscriber implements Subscriber {
    private volatile TimetableDay result;
    private final int secondsForWaitNotification;
    private final String expectedGroupName;

    public TemporarySubscriber(int secondsForWaitNotification, String expectedGroupName) {
        this.secondsForWaitNotification = secondsForWaitNotification;
        this.expectedGroupName = expectedGroupName;
        this.result = null;
        async(this::startDaemon);
    }

    private void startDaemon(){
       trySleep(TimeUnit.SECONDS.toMillis(secondsForWaitNotification));
       setResultToNone();
    }

    private void trySleep(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setResultToNone(){
        result = TimetableDay.builder()
                .date("NOT")
                .build();
    }

    @Override
    public void handleNotification(String id, TimetableDay chagedTimetableDay) {
        if(Objects.equals(id, expectedGroupName))
            result = chagedTimetableDay;
    }

    public TimetableDay get(){
        sleepWhileResultEqualsNull();
        if(Objects.equals(result.getDate(), "NOT"))
            return null;
        return result;
    }

    private void sleepWhileResultEqualsNull(){
        while (result == null)
            Thread.onSpinWait();
    }
}
