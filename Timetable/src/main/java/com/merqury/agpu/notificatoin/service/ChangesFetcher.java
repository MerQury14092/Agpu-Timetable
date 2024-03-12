package com.merqury.agpu.notificatoin.service;

import com.merqury.agpu.ServerStates;
import com.merqury.agpu.DTO.TimetableDay;
import com.merqury.agpu.DTO.Groups;
import com.merqury.agpu.enums.TimetableOwner;
import com.merqury.agpu.memory.TimetableMemory;
import com.merqury.agpu.service.GetSearchIdService;
import com.merqury.agpu.service.GetTimetableService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static com.merqury.agpu.general.Foundation.*;

@Component
@Log4j2
public class ChangesFetcher {
    private final GetTimetableService getTimetableService;
    private final GetSearchIdService getSearchIdService;
    private final TimetableMemory timetableMemory;
    private final TimetableChangesPublisher timetableChangesPublisher;

    @Autowired
    public ChangesFetcher(
            GetTimetableService getTimetableService,
            GetSearchIdService getSearchIdService,
            TimetableMemory timetableMemory
    ){
        this.getTimetableService = getTimetableService;
        this.getSearchIdService = getSearchIdService;
        this.timetableMemory = timetableMemory;
        this.timetableChangesPublisher = TimetableChangesPublisher.singleton();
        startDaemonForRegularTimetableFetching();
    }

    private void startDaemonForRegularTimetableFetching(){
        async(() -> {
            while (true){
                while (!ServerStates.isGroupUpdated)
                    ;
                ServerStates.isTimetableFetched = false;
                tryFetchTimetable();
                ServerStates.isTimetableFetched = true;
                waitHours();
            }
        });
    }

    private void tryFetchTimetable(){
        try {
            fetchTimetable();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void fetchTimetable() throws IOException {
        log.info("Start fetching timetable");
        long timestampOfStartFetching = System.currentTimeMillis();
        for(Groups faculty: getSearchIdService.getAllGroupsFromMainPage())
            fetchTimetableForFaculty(faculty);
        log.info("Fetching of timetable changes complete on {} ms", System.currentTimeMillis() - timestampOfStartFetching);
    }

    private void fetchTimetableForFaculty(Groups faculty) throws IOException {
        for (String group: faculty.getGroups())
            fetchTimetableForGroup(group);
    }

    private void fetchTimetableForGroup(String groupName) throws IOException {
        checkChangesByDate(groupName, getToday());
        checkChangesByDateAfter(TimeUnit.SECONDS.toMillis(15), groupName, getTomorrow());
    }

    private void tryToCheckChangesByDate(String groupName, String date){
        try {
            checkChangesByDate(groupName, date);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkChangesByDate(String groupName, String date) throws IOException {
        TimetableDay fromMemory = timetableMemory.getTimetableByDate(groupName, date, TimetableOwner.GROUP);
        if(fromMemory.isSynthetic)
            return;
        TimetableDay fromSite = getTimetableService.getTimetableDayFromSite(groupName, date, TimetableOwner.GROUP);
        checkDayChanges(fromSite, fromMemory);
    }

    private void checkChangesByDateAfter(long milliseconds, String groupName, String date){
        async(() -> {
            trySleep(milliseconds);
            tryToCheckChangesByDate(groupName, date);
        });
    }

    private void trySleep(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getToday(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.now().format(formatter);
    }

    private String getTomorrow(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDate.now().plusDays(1).format(formatter);
    }

    private void checkDayChanges(TimetableDay timetableDay, TimetableDay timetableDayFromMemory){
        if(!timetableDayFromMemory.equals(timetableDay) && !timetableDayFromMemory.getDisciplines().isEmpty()){
            timetableChangesPublisher.publishNotification(timetableDay.getId(), timetableDay);
            timetableMemory.addDiscipline(timetableDay);
        }
    }

    private void waitHours(){
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(30));
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
}
