package com.merqury.agpu.memory;

import com.merqury.agpu.DTO.Groups;
import com.merqury.agpu.ServerStates;
import com.merqury.agpu.enums.TimetableOwner;
import com.merqury.agpu.service.GetSearchIdService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.merqury.agpu.general.Foundation.*;

@Component
@Log4j2
public class GroupIdMemory implements IdMemory{
    private final GetSearchIdService groupSearchIdService;

    private final HashMap<String, Integer> groupSearchIdMap;

    public static boolean isUpdatedAllGroups;

    @Override
    public int getSearchId(String groupName){
        if(isUpdatedAllGroups)
            return groupSearchIdMap.get(groupName);
        return 0;
    }

    public GroupIdMemory(GetSearchIdService groupSearchIdService) {
        this.groupSearchIdService = groupSearchIdService;
        this.groupSearchIdMap = new HashMap<>();
        startDaemonForUpdatingGroups();
    }

    private void startDaemonForUpdatingGroups(){
        async(() -> {
            while (true){
                ServerStates.isGroupUpdated = false;
                fetchData();
                ServerStates.isGroupUpdated = true;
                waitWeek();
            }
        });
    }


    private void fetchData(){
        groupSearchIdMap.clear();
        isUpdatedAllGroups = false;
        log.info("start updating group names");
        fetchDataFromService();
        isUpdatedAllGroups = true;
        log.info("group names is updated");
    }

    private void fetchDataFromService(){
        for(Groups faculty: groupSearchIdService.getAllGroupsFromMainPage())
            fetchGroupsFromFaculty(faculty);
    }

    private void fetchGroupsFromFaculty(Groups faculty){
        for(String groupName: faculty.getGroups())
            putInMemory(groupName);
    }

    private void putInMemory(String groupName){
        int searchId = groupSearchIdService.getSearchId(groupName, TimetableOwner.GROUP);
        groupSearchIdMap.put(groupName, searchId);
    }

    private void waitWeek(){
        try {
            Thread.sleep(TimeUnit.DAYS.toMillis(7));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
