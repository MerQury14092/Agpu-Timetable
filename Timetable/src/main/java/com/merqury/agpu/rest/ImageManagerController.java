package com.merqury.agpu.rest;

import com.merqury.agpu.DTO.Discipline;
import com.merqury.agpu.DTO.ImageUrl;
import com.merqury.agpu.DTO.TimetableDay;
import com.merqury.agpu.enums.DisciplineType;
import com.merqury.agpu.general.Controllers;
import com.merqury.agpu.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


@RestController
@RequestMapping("/api/${api.version}/timetable/image")
public class ImageManagerController {
    private final ImageService service;

    public ImageManagerController(ImageService service) {
        this.service = service;
    }

    @PostMapping(value = "/discipline")
    public ImageUrl discipline(
            @RequestBody Discipline[] disc,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws IOException {
        Map<DisciplineType, String> types = extractMappingForDisciplineType(request);
        Map<DisciplineType, String> colors = extractMappingForDisciplineTypeColors(request);
        int cellWidth = 600;
        if(request.getParameter("cell_width") != null && isDigit(request.getParameter("cell_width")) && request.getParameter("cell_width").length() < 5)
            cellWidth = Integer.parseInt(request.getParameter("cell_width"));
        if(disc.length == 2) {
            BufferedImage res = service.getImageByTimetableOfSubDiscipline(disc[0], disc[1], cellWidth, types, colors);
            return service.saveImageAngGetUrl(res);
        }
        else if(disc.length == 1) {
            BufferedImage res = service.getImageByTimetableOfDiscipline(disc[0], cellWidth, types, colors);
            return service.saveImageAngGetUrl(res);
        }
        response.sendError(400);
        return null;
    }

    @PostMapping(value = "/day")
    public ImageUrl day(
            @RequestBody TimetableDay timetableDay,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws IOException {
        if (checkOrientation(response, request)) return null;
        if (request.getParameter("font") != null)
            service.loadFont(request.getParameter("font"));
        Map<DisciplineType, String> types = extractMappingForDisciplineType(request);
        Map<DisciplineType, String> colors = extractMappingForDisciplineTypeColors(request);
        int cellWidth = 600;
        if(request.getParameter("cell_width") != null && isDigit(request.getParameter("cell_width")) && request.getParameter("cell_width").length() < 5)
            cellWidth = Integer.parseInt(request.getParameter("cell_width"));
        if(request.getParameter("vertical") == null) {
            BufferedImage res = service.getImageByTimetableOfDayHorizontal(timetableDay, cellWidth, false, types, colors);
            return service.saveImageAngGetUrl(res);
        }
        BufferedImage res = service.getImageByTimetableOfDayVertical(timetableDay, cellWidth, false, types, colors);
        return service.saveImageAngGetUrl(res);
    }

    @PostMapping(value = "/6days")
    public ImageUrl days(
            @RequestBody TimetableDay[] timetableDays,
            HttpServletResponse response,
            HttpServletRequest request
    ) throws IOException {
        if (request.getParameter("font") != null)
            service.loadFont(request.getParameter("font"));
        if (checkOrientation(response, request)) return null;
        if(timetableDays.length > 6 || timetableDays.length == 0){
            Controllers.sendError(416, "Expected array of [1; 6] days", response);
        }
        if(timetableDays.length < 6){
            TimetableDay[] oldTimetableDays = new TimetableDay[timetableDays.length];
            System.arraycopy(timetableDays, 0, oldTimetableDays, 0, timetableDays.length);

            timetableDays = new TimetableDay[6];
            System.arraycopy(oldTimetableDays, 0, timetableDays, 0, oldTimetableDays.length);
            for (int i = oldTimetableDays.length; i < timetableDays.length; i++) {
                String currentDateStr = oldTimetableDays[0].getDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate date = LocalDate.parse(currentDateStr, formatter);
                currentDateStr = date.plusDays(i).format(formatter);
                timetableDays[i] = TimetableDay.builder()
                        .disciplines(List.of())
                        .date(currentDateStr)
                        .build();
            }
        }

        Map<DisciplineType, String> types = extractMappingForDisciplineType(request);
        Map<DisciplineType, String> colors = extractMappingForDisciplineTypeColors(request);
        int cellWidth = 600;
        if(request.getParameter("cell_width") != null && isDigit(request.getParameter("cell_width")) && request.getParameter("cell_width").length() < 5)
            cellWidth = Integer.parseInt(request.getParameter("cell_width"));
        if(request.getParameter("vertical") != null){
            BufferedImage res = service.getImageByTimetableOf6DaysVertical(timetableDays, cellWidth, false, types, colors);
            return service.saveImageAngGetUrl(res);
        }

        if(request.getParameter("vertical") == null) {
            BufferedImage res = service.getImageByTimetableOf6DaysHorizontal(timetableDays, cellWidth, false, types, colors);
            return service.saveImageAngGetUrl(res);
        }
        return null;
    }

    private boolean checkOrientation(HttpServletResponse response, HttpServletRequest request) throws IOException {
        if(request.getParameter("vertical") == null && request.getParameter("horizontal") == null) {
            Controllers.sendError(400, "Expected 'vertical' or 'horizontal' in query params", response);
            return true;
        }
        else if(request.getParameter("vertical") != null && request.getParameter("horizontal") != null) {
            Controllers.sendError(409, "Expected 'vertical' or 'horizontal' in query params", response);
            return true;
        }
        return false;
    }

    private Map<DisciplineType, String> extractMappingForDisciplineType(HttpServletRequest request){
        Map<DisciplineType, String> res = new HashMap<>();
        for(DisciplineType type: DisciplineType.values())
            if(request.getParameter(type.name()+"_text") != null)
                res.put(type, request.getParameter(type.name()+"_text"));
        return res;
    }

    private Map<DisciplineType, String> extractMappingForDisciplineTypeColors(HttpServletRequest request){
        Map<DisciplineType, String> res = new HashMap<>();
        for(DisciplineType type: DisciplineType.values())
            if(request.getParameter(type.name()+"_color") != null) {
                Pattern color_pattern = Pattern.compile("^#[0123456789abcdef]{6}$");
                if(color_pattern.matcher(request.getParameter(type.name()+"_color")).matches())
                    res.put(type, request.getParameter(type.name() + "_color"));
            }
        return res;
    }

    private boolean isDigit(String str){
        for(char cur: str.toCharArray())
            if(!Character.isDigit(cur))
                return false;
        return true;
    }

}
