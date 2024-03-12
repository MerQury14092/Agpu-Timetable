package com.merqury.agpu.rest;

import com.merqury.agpu.DTO.Groups;
import com.merqury.agpu.DTO.TimetableDay;
import com.merqury.agpu.DTO.Week;
import com.merqury.agpu.enums.TimetableOwner;
import com.merqury.agpu.general.Controllers;
import com.merqury.agpu.memory.StatMemory;
import com.merqury.agpu.service.GetSearchIdService;
import com.merqury.agpu.service.GetTimetableService;
import com.merqury.agpu.service.GetWeeksService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@AllArgsConstructor
@RequestMapping("/api/timetable")
public class TimetableController {
    private final GetTimetableService service;
    private final GetSearchIdService searchIdService;
    private final GetWeeksService weeksService;
    private final String dateRegex = "^(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[0-2])\\.\\d{4}$";

    @GetMapping("/day")
    public TimetableDay getTimetable(HttpServletRequest request,
                                     @PathParam("") String date,
                                     @PathParam("") TimetableOwner owner,
                                     HttpServletResponse response
    ) throws IOException {
        System.out.println("Request for: " + (request.getParameter("id") == null ? request.getParameter("groupId") : request.getParameter("id")));
        if (date == null || (request.getParameter("groupId") == null && request.getParameter("id") == null)) {
            if (date == null) {
                Controllers.sendError(400, "Expected date", response);
                return null;
            }
            Controllers.sendError(400, "id", response);
            return null;
        }
        if (!Pattern.matches(dateRegex, date))
            date = "Invalid date format";
        String groupName = searchIdService.getFullGroupName(request.getParameter("id") == null ? request.getParameter("groupId") : request.getParameter("id"));

        if (date.equals("Invalid date format")) {
            Controllers.sendError(400, date, response);
            return null;
        }

        if(searchIdService.getSearchId(request.getParameter("id"), owner) == 0){
            Controllers.sendError(400, "Unknown id", response);
            return null;
        }
        TimetableDay res = (
                service.getTimetableDayFromMemoryOrSiteAndCacheIfNeedIt(request.getParameter("id") == null ?
                        request.getParameter("groupId") :
                        request.getParameter("id"), date, owner)
        ).deleteHolidays();
        if (res.getId() == null)
            res.setId(groupName);
        else if (res.getId().equals("None"))
            res.setId("Unknown group");
        StatMemory.currentStatistics.addRecord(owner, searchIdService.getSearchContent(request.getParameter("id"), switch (owner){
            case GROUP -> "Group";
            case TEACHER -> "Teacher";
            case CLASSROOM -> "Classroom";
        }));
        return res;
    }

    @GetMapping("/days")
    public List<TimetableDay> getTimetable(
            @PathParam("") String startDate,
            @PathParam("") String endDate,
            @PathParam("") TimetableOwner owner,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        System.out.println("Request for: " + (request.getParameter("id") == null ? request.getParameter("groupId") : request.getParameter("id")));
        if (startDate == null || endDate == null || (request.getParameter("groupId") == null && request.getParameter("id") == null)) {
            String groupIdMessage = request.getParameter("id") == null ? request.getParameter("groupId") : request.getParameter("id");
            if (startDate == null) {
                Controllers.sendError(400, "Expected start date", response);
                return null;
            }
            if (endDate == null) {
                Controllers.sendError(400, "Expected end date", response);
                return null;
            }
            if (request.getParameter("groupId") == null && request.getParameter("id") == null) {
                Controllers.sendError(400, "id", response);
                return null;
            }
            return List.of(
                    TimetableDay.builder()
                            .date(startDate)
                            .id(groupIdMessage)
                            .disciplines(List.of())
                            .build()
            );
        }
        if(searchIdService.getSearchId(request.getParameter("id"), owner) == 0){
            Controllers.sendError(400, "Unknown id", response);
            return null;
        }
        if (!Pattern.matches(dateRegex, startDate)) {
            Controllers.sendError(400, "Invalid date format (start date)", response);
            return null;
        }
        if (!Pattern.matches(dateRegex, endDate)) {
            Controllers.sendError(400, "Invalid date format (end date)", response);
            return null;
        }
        String groupName = searchIdService.getFullGroupName(request.getParameter("id") == null ? request.getParameter("groupId") : request.getParameter("id"));
        if (groupName.equals("None") && owner.equals(TimetableOwner.GROUP)) {
            Controllers.sendError(400, "Unknown group", response);
            return null;
        }
        if (!groupName.trim().equals(request.getParameter("id") == null ? request.getParameter("groupId") : request.getParameter("id")) && owner.equals(TimetableOwner.GROUP)) {
            Controllers.sendError(400, "Unknown group", response);
            return null;
        }


        boolean removeNull = (request.getParameter("removeEmptyDays") != null);
        List<TimetableDay> result = service.getDisciplines(request.getParameter("id") == null ? request.getParameter("groupId") : request.getParameter("id"), owner, startDate, endDate);
        result.forEach(TimetableDay::deleteHolidays);
        if (removeNull)
            result.removeIf(TimetableDay::isEmpty);
        if (result.isEmpty())
            return List.of();
        if (result.get(0).getId() == null)
            result.get(0).setId(groupName);
        else if (result.get(0).getId().equals("None"))
            return List.of();
        StatMemory.currentStatistics.addRecord(owner, searchIdService.getSearchContent(request.getParameter("id"), switch (owner){
            case GROUP -> "Group";
            case TEACHER -> "Teacher";
            case CLASSROOM -> "Classroom";
        }).split(",")[0]);
        return result;

    }

    @GetMapping("/weeks")
    public List<Week> getWeeks() {
        return weeksService.getEverything();
    }

    @GetMapping("/groups")
    public List<Groups> groups() {
        return searchIdService.getAllGroupsFromMainPage();
    }
}
