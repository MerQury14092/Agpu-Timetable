package com.merqury.agpu.memory;

import com.merqury.agpu.DTO.Discipline;
import com.merqury.agpu.DTO.TimetableDay;
import com.merqury.agpu.enums.TimetableOwner;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.merqury.agpu.general.Foundation.*;

@Component
@Log4j2
public class TimetableMemory {
    private final List<TimetableDay> memory;
    private static final long memoryExpirationTime;

    static {
        memoryExpirationTime = TimeUnit.MINUTES.toMillis(35);
    }

    public TimetableMemory(){
        memory = new ArrayList<>();
    }

    public void addDiscipline(TimetableDay timetableDay){
        addingStubIfTimetableDayIsEmpty(timetableDay);
        if(!getTimetableByDate(timetableDay.getId(), timetableDay.getDate(), timetableDay.getOwner()).equals(timetableDay))
            memory.removeIf(day -> day.getId().equals(timetableDay.getDate()) && day.getDate().equals(timetableDay.getDate()));
        memory.add(timetableDay);
        addTaskForCleanDayFromMemory(timetableDay);
    }

    private void addingStubIfTimetableDayIsEmpty(TimetableDay day){
        if (day.isEmpty())
            day.getDisciplines().add(Discipline.holiday());
    }

    private void addTaskForCleanDayFromMemory(TimetableDay timetableDay){
        log.debug("в память добавлен день - {}", timetableDay);
        if(timetableDay.isSynthetic)
            return;
        async(() -> {
            trySleep();
            memory.remove(timetableDay);
        });
    }

    private void trySleep(){
        try {
            Thread.sleep(TimetableMemory.memoryExpirationTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public TimetableDay getTimetableByDate(String id, String date, TimetableOwner owner){
        log.debug("запрос из памяти на {} - {}", id, date);
        var res =  getFilteredByIdAndDateTimetableDays(id, date);
        if(!res.isEmpty())
            return res.get(0);
        return getEmptyDateWith(id, date, owner);
    }

    private List<TimetableDay> getFilteredByIdAndDateTimetableDays(String id, String date){
        return memory.stream()
                .filter(day -> (day.getId().equals(id) && day.getDate().equals(date)))
                .toList();
    }

    private TimetableDay getEmptyDateWith(String id, String date, TimetableOwner owner){
        return TimetableDay.builder()
                .date(date)
                .id(id)
                .owner(owner)
                .disciplines(Collections.emptyList())
                .build();
    }
}
