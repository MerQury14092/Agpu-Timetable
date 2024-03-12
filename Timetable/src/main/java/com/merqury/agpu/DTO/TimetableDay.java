package com.merqury.agpu.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.merqury.agpu.enums.TimetableOwner;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimetableDay {
    String date;
    String id;
    TimetableOwner owner;
    List<Discipline> disciplines;

    @JsonIgnore
    public boolean isSynthetic;

    @JsonIgnore
    public boolean isEmpty(){
        return disciplines.isEmpty();
    }

    public TimetableDay deleteHolidays(){
        disciplines = disciplines.stream()
                .filter(discipline -> !discipline.getName().equals("HOLIDAY"))
                .collect(Collectors.toList());
        return this;
    }

    public TimetableDay proxy(){
        List<Discipline> proxyList = new ArrayList<>();

        for (Discipline disc: disciplines) {
            Discipline proxyDisc;
            try {
                proxyDisc = disc.proxy();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            proxyList.add(proxyDisc);
        }

        return TimetableDay.builder()
                .date(date)
                .id(id)
                .owner(owner)
                .disciplines(proxyList)
                .build();
    }
}
