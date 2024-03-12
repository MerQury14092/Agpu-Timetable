package com.merqury.agpu.DTO;

import com.merqury.agpu.enums.TimetableOwner;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Data
public class Statistics {
    private String startTime, endTime, date;
    private int total;
    private Map<TimetableOwner, Map<String, Integer>> statistics;
    public Statistics(){
        this.startTime = getCurrentTime();
        this.endTime = "now";
        date = getCurrentDate();
        statistics = new HashMap<>();
        for(TimetableOwner owner: TimetableOwner.values()){
            statistics.put(owner, new HashMap<>());
        }
    }

    public void endRecording(){
        endTime = getCurrentTime();
    }

    private static String getCurrentTime(){
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return time.format(formatter);
    }

    private static String getCurrentDate(){
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }

    public void addRecord(TimetableOwner owner, String group){
        if(!statistics.get(owner).containsKey(group))
            statistics.get(owner).put(group, 1);
        else
            statistics.get(owner).put(group, statistics.get(owner).get(group)+1);
        total++;
    }
}
