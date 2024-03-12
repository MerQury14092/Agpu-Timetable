package com.merqury.agpu.DTO;

import static java.lang.Integer.parseInt;

public class DateString implements Comparable<DateString>{
    private final int day;
    private final int month;
    private final int year;

    public DateString(String date){
        String[] d = date.split("\\.");
        day = parseInt(d[0]);
        month = parseInt(d[1]);
        year = parseInt(d[2]);
    }

    public static DateString build(String date){
        return new DateString(date);
    }

    @Override
    public String toString() {
        return String.format("%02d.%02d.%02d",day, month, year);
    }

    @Override
    public int compareTo(DateString o) {
        if(o.day == day && o.month == month && o.year == year)
            return 0;
        if(o.year > year)
            return -1;
        if(year > o.year)
            return 1;
        if(o.month > month)
            return -1;
        if(month > o.month)
            return 1;
        if(o.day > day)
            return -1;
        return 1;
    }
}
