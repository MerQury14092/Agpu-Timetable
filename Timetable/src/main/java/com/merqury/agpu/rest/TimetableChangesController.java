package com.merqury.agpu.rest;

import com.merqury.agpu.general.Controllers;
import com.merqury.agpu.DTO.TimetableDay;
import com.merqury.agpu.DTO.Groups;
import com.merqury.agpu.memory.TimetableMemory;
import com.merqury.agpu.notificatoin.DTO.Notification;
import com.merqury.agpu.notificatoin.DTO.Webhook;
import com.merqury.agpu.notificatoin.Webhooks;
import com.merqury.agpu.notificatoin.interfaces.TemporarySubscriber;
import com.merqury.agpu.notificatoin.service.WebhookRegistryService;
import com.merqury.agpu.service.GetSearchIdService;
import com.merqury.agpu.notificatoin.service.TimetableChangesPublisher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@RestController
@RequestMapping("/api/${api.version}/timetable/changes")
public class TimetableChangesController {
    private final GetSearchIdService getSearchIdService;
    private final TimetableChangesPublisher changesPublisher;
    private final TimetableMemory memory;
    private final WebhookRegistryService webhookRegistryService;
    private final MessageDigest digest;

    public TimetableChangesController(GetSearchIdService getSearchIdService, TimetableMemory memory, MessageDigest digest) {
        this.getSearchIdService = getSearchIdService;
        this.memory = memory;
        this.digest = digest;
        webhookRegistryService = WebhookRegistryService.singleton();
        this.changesPublisher = TimetableChangesPublisher.singleton();
    }


    @GetMapping("/day")
    public TimetableDay getChanges(
            @PathParam("") String groupId,
            HttpServletResponse response,
            @PathParam("") Optional<Integer> timeout
    ) throws IOException
    {
        if(timeout.isEmpty()){
            Controllers.sendError(400, "Expected timeout", response);
            return null;
        }
        if(groupId == null){
            Controllers.sendError(400, "Expected groupId", response);
            return null;
        }
        boolean contains = false;
        Super:
        for(Groups faculty: getSearchIdService.getAllGroupsFromMainPage())
            for (String group: faculty.getGroups())
                if(group.equals(groupId)){
                    contains = true;
                    break Super;
                }
        if(!contains){
            Controllers.sendError(400, "Unknown group", response);
            return null;
        }

        TemporarySubscriber subscriber = new TemporarySubscriber(timeout.get(), groupId);
        changesPublisher.addSubscriber(subscriber);
        TimetableDay res = subscriber.get();
        if(res == null){
            changesPublisher.removeSubscriber(subscriber);
            return null;
        }
        changesPublisher.removeSubscriber(subscriber);
        return res.deleteHolidays();
    }

    @PostMapping("/push")
    public String pushNotification(HttpServletRequest request, HttpServletResponse response, @RequestBody TimetableDay timetableDay) throws IOException {
        String authToken = request.getHeader("Authorization");
        if(authToken == null) {
            Controllers.sendError(403, "Forbidden", response);
            return null;
        }
        String encryptedAuthToken = new String(digest.digest(authToken.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        if(!encryptedAuthToken.equals("=�\u001E>Gg\u001EUmù�*�rX�㯘FްВ�i����")) {
            Controllers.sendError(403, "Forbidden", response);
            return null;
        }
        timetableDay.isSynthetic = true;
        memory.addDiscipline(timetableDay);
        changesPublisher.publishNotification(timetableDay.getId(), timetableDay);
        return "OK";
    }

    @GetMapping("/day/check")
    public Notification checkChanges(
            @PathParam("") String groupId,
            HttpServletResponse response,
            @PathParam("") Optional<Integer> timeout
    ) throws IOException
    {
        TimetableDay timetableDay = getChanges(groupId, response, timeout);
        if(timetableDay == null)
            return Notification.noChanges("N/A");
        return Notification.thereAreChanges(timetableDay.getDate());
    }

    @PostMapping(value = "webhook/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerWebhook(@RequestBody Webhook webhook, HttpServletResponse response) throws IOException {
        boolean contains = false;
        Super:
        for(Groups faculty: getSearchIdService.getAllGroupsFromMainPage())
            for (String group: faculty.getGroups())
                if(group.equals(webhook.getGroup())){
                    contains = true;
                    break Super;
                }
        if(!contains){
            Controllers.sendError(400, "Unknown group", response);
            return null;
        }
        if(!Webhooks.ping(webhook.getUrl())){
            Controllers.sendError(400, "Webhook did not return the expected response within 5 secs", response);
            return null;
        }
        int result = webhookRegistryService.addWebhook(webhook);
        if (result == 0)
            return """
                    {
                        "status": "OK"
                    }
                    """;

        Controllers.sendError(400, "webhook already registered", response);
        return null;
    }

}
