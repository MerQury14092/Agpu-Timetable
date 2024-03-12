package com.merqury.agpu.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.SortedMap;
import java.util.TreeMap;

@Data
public class Week {
    private int id;
    private String from;
    private String to;
    private SortedMap<DateString, String> dayNames;

    public void initArray(){
        dayNames = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate from = LocalDate.parse(this.from, formatter);
        dayNames.put(DateString.build(from.format(formatter)), "Понедельник");
        from = from.plusDays(1);
        dayNames.put(DateString.build(from.format(formatter)), "Вторник");
        from = from.plusDays(1);
        dayNames.put(DateString.build(from.format(formatter)), "Среда");
        from = from.plusDays(1);
        dayNames.put(DateString.build(from.format(formatter)), "Четверг");
        from = from.plusDays(1);
        dayNames.put(DateString.build(from.format(formatter)), "Пятница");
        from = from.plusDays(1);
        dayNames.put(DateString.build(from.format(formatter)), "Суббота");
        from = from.plusDays(1);
        dayNames.put(DateString.build(from.format(formatter)), "Воскресенье");
    }
}
