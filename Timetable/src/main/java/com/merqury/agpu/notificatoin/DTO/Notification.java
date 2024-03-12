package com.merqury.agpu.notificatoin.DTO;

import lombok.Data;

@Data
public class Notification {
    private boolean thereAreChanges;
    private String date;

    public static Notification noChanges(String date){
        return new Notification(false, date);
    }

    public static Notification thereAreChanges(String date){
        return new Notification(true, date);
    }

    private Notification(boolean hadChanges, String date){
        this.date = date;
        this.thereAreChanges = hadChanges;
    }
}
