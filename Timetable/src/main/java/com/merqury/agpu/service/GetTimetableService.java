package com.merqury.agpu.service;

import com.merqury.agpu.DTO.TimetableDay;
import com.merqury.agpu.DTO.Discipline;
import com.merqury.agpu.enums.TimetableOwner;
import com.merqury.agpu.enums.DisciplineType;
import com.merqury.agpu.memory.GroupIdMemory;
import com.merqury.agpu.memory.IdMemory;
import com.merqury.agpu.memory.TimetableMemory;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Log4j2
public class GetTimetableService {
    private final TimetableMemory timetableMemory;
    private final GetSearchIdService getSearchIdService;
    private final Map<TimetableOwner, IdMemory> idMemoryMap;

    public GetTimetableService(
            GroupIdMemory groupIdMemory, TimetableMemory memory, GetSearchIdService getSearchIdService
    ) {
        this.timetableMemory = memory;
        this.getSearchIdService = getSearchIdService;
        idMemoryMap = Map.of(
                TimetableOwner.GROUP, groupIdMemory
        );
    }

    public List<TimetableDay> getDisciplines(
            String id, TimetableOwner owner, String startDate, String endDate
    ) throws IOException {
        List<TimetableDay> result = new ArrayList<>();
        for(String date: getDatesBetween(startDate, endDate))
            result.add(getTimetableDayFromSite(id, date, owner));
        return result;
    }

    public TimetableDay getTimetableDayFromMemoryOrSiteAndCacheIfNeedIt(
            String id, String date, TimetableOwner owner
    ) throws IOException {
        String ownerName = owner.name().toLowerCase();
        ownerName = Character.toUpperCase(ownerName.charAt(0))+ownerName.substring(1);
        id = getSearchIdService.getSearchContent(id, ownerName);
        TimetableDay res = getTimetableDayFromMemory(id, date, owner);
        if(res.getId().equals("None")) {
            TimetableDay[] week = getTimetableWeek(id, date, owner);
            for (TimetableDay timetableDay : week) {
                timetableMemory.addDiscipline(timetableDay);
            }
            res = getFromArrayByDate(week, date);
            timetableMemory.addDiscipline(res);
        }
        return res;
    }

    public TimetableDay getTimetableDayFromSite(
            String id, String date, TimetableOwner owner
    ) throws IOException {
        TimetableDay[] week = getTimetableWeek(id, date, owner);
        return getFromArrayByDate(week, date);
    }

    public TimetableDay getTimetableDayFromMemory(
            String id, String date, TimetableOwner owner
    ) {
        TimetableDay dayFromMemory = timetableMemory.getTimetableByDate(id, date, owner);
        if(!dayFromMemory.getDisciplines().isEmpty())
            return dayFromMemory;
        return TimetableDay.builder().id("None").date(date).disciplines(List.of()).owner(owner).build();
    }

    public TimetableDay[] getTimetableWeek(String id, String date, TimetableOwner owner) throws IOException {
        String html = getHtmlFromPage(id, getSearchIdFromCacheOrSite(id, owner), owner, weekIdByDate(date));
        TimetableDay[] week = parseHtml(html, owner);

        Arrays.stream(week).forEach(day ->
            day.getDisciplines().removeIf(discipline -> discipline.getName() == null)
        );

        return week;
    }

    private int getSearchIdFromCacheOrSite(String id, @NotNull TimetableOwner owner){
        if(idMemoryMap.containsKey(owner))
            return idMemoryMap.get(owner).getSearchId(id);
        return getSearchIdService.getSearchId(id, owner);
    }

    private TimetableDay getFromArrayByDate(TimetableDay[] arr, String date){
        return Arrays.stream(arr)
                .filter(el -> el.getDate().equals(date))
                .findFirst()
                .orElseThrow();
    }

    private long weekIdByDate(String date){
        int mappingWeekId = 3655;

        return mappingWeekId + countDays(date) / 7;
    }

    private String getHtmlFromPage(
            String searchText, int searchId, TimetableOwner owner, long weekId
    ) throws IOException {
        String ownerString = capitalize(owner.name().toLowerCase());
        URL url1 = new URL(
                """
                        http://www.it-institut.ru/Raspisanie/SearchedRaspisanie\
                        ?OwnerId=118&SearchId=%d&SearchString=%s&Type=%s&WeekId=%d""".formatted(
                        searchId,
                        URLEncoder.encode(searchText, StandardCharsets.UTF_8),
                        ownerString,
                        weekId
                )
        );

        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();

        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        InputStream is = conn.getInputStream();


        StringBuilder html = new StringBuilder();

        Scanner sc = new Scanner(is);

        String cur = "";
        while (!cur.equals("</html>")) {
            cur = sc.nextLine();
            html.append(cur);
        }

        return html.toString();
    }

    private String capitalize(String str){
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private TimetableDay[] parseHtml(String html, TimetableOwner for_who){
        TimetableDay[] week = new TimetableDay[7];
        Document doc = Jsoup.parse(html);

        Elements elements = doc
                .getElementsByClass("table")
                .first()
                .getElementsByTag("tbody")
                .first()
                .getElementsByTag("tr");

        Elements times = doc
                .getElementsByClass("thead-light")
                .first()
                .getElementsByTag("tr")
                .first()
                .getElementsByTag("th");

        List<Element> dayCells = doc
                .getElementsByClass("table")
                .first()
                .getElementsByTag("tbody")
                .first()
                .getElementsByTag("tr")
                .stream().map(element -> element
                        .getElementsByTag("th")
                        .first())
                .toList();


        List<String> dates = dayCells.stream().map( element ->
                element.html().split("<br>")[1].trim()
        ).toList();

        String id = doc
                .getElementsByClass("input-group")
                .first()
                .getElementsByTag("input")
                .first()
                .attr("value");

        for (int i = 0; i < week.length; i++) {
            week[i] = TimetableDay.builder()
                    .date(dates.get(i))
                    .disciplines(new ArrayList<>())
                    .id(id)
                    .build();
        }

        Integer[] col = parseCol(times);

        for (int i = 0; i < 7; i++)
            parseDay(elements.get(i), week[i], col);

        for (TimetableDay timetableDay : week) {
            timetableDay.setOwner(for_who);
        }

        return week;
    }

    public static List<String> getDatesBetween(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        List<String> dates = new ArrayList<>();
        LocalDate dateCursor = start;

        while (!dateCursor.isAfter(end)) {
            dates.add(dateCursor.format(formatter));
            dateCursor = dateCursor.plusDays(1);
        }

        return dates;
    }

    private long countDays(String endDate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    LocalDate dt = LocalDate.parse("28.08.2023", formatter);
    long mappingWeek = dt.toEpochDay();

    dt = LocalDate.parse(endDate, formatter);
    long currentWeek = dt.toEpochDay();

    return (currentWeek - mappingWeek);
}

    private void assignTimeOfDisciplines(List<Discipline> result, Integer[] col) {
        Integer[] pairs = new Integer[result.size()];
        for (int i = 0; i < pairs.length; i++) {
            if(result.get(i) == null) {
                pairs[i] = col[i];
                continue;
            }
            pairs[i] = result.get(i).getColspan();
        }

        Integer[] res = res(col, pairs);

        for (int i = 0; i < result.size(); i++) {
            if(result.get(i) == null)
                continue;
            result.get(i).setTime(timeByIndex(res[i]));
        }
    }

    private Integer[] parseCol(Elements elements){
        List<Integer> res = new ArrayList<>();
        for(Element el: elements)
            if(el.hasAttr("colspan"))
                res.add(Integer.parseInt(el.attr("colspan")));
        return res.toArray(new Integer[0]);
    }

    private void parseDay(Element el, TimetableDay day, Integer[] col) { // сюда приходит тег tr
        Element nameOfClass = el.getElementsByTag("th").first();
        Elements disciplines = el.getElementsByTag("td");
        for (int i = 0; i < 8; i++) {
            assert nameOfClass != null;
            parseDiscipline(disciplines.get(i), day, nameOfClass.html().split("\n")[1]);
        }
        assignTimeOfDisciplines(day.getDisciplines(), col);
    }

    private void parseDiscipline(Element el, TimetableDay day, String date) { // сюда приходит тег td
        Discipline result = new Discipline();
        result.setDistant(el.text().contains("Дистанционно"));
        Elements spans = el.getElementsByTag("span");
        if(spans.isEmpty()) {
            result.setDate(date);
            result.setColspan(Integer.parseInt(el.attr("colspan")));
            day.getDisciplines().add(result);
            return;
        }
        String name = spans.get(0).text();
        List<Character> characters = List.of(',', '.');
        if(characters.contains(name.charAt(name.length()-1)))
            name = name.substring(0, name.length()-1);
        result.setName(name);
        String[] prepodAndAudience = spans.get(1).text().split(",");
        if (prepodAndAudience.length >= 3) {
            result.setTeacherName(prepodAndAudience[0].trim());
            result.setAudienceId(prepodAndAudience[prepodAndAudience.length-1].trim());
        } else if (prepodAndAudience.length == 2) {
            result.setTeacherName(prepodAndAudience[0].trim());
            result.setAudienceId(prepodAndAudience[1].trim());
        }
        if (spans.size() < 4)
            result.setSubgroup(0);
        else
            result.setSubgroup(spans.get(3).text().contains("1") ? 1 : 2);
        result.setDate(date);
        result.setGroupName(spans.get(2).text().replace("(", "").replace(")",""));
        result.setColspan(Integer.parseInt(el.attr("colspan")));
        assignTypeAndRenameDiscipline(result);
        day.getDisciplines().add(result);
    }

    private void assignTypeAndRenameDiscipline(Discipline discipline){
        Map<String, DisciplineType> mapBetweenNameAndType = Map.of(
                "конс", DisciplineType.cons,
                "лек", DisciplineType.lec,
                "фэпо", DisciplineType.fepo,
                "зач", DisciplineType.cred,
                "выходной", DisciplineType.hol,
                "каникулы", DisciplineType.hol,
                "лаб", DisciplineType.lab,
                "экз", DisciplineType.exam,
                "прак", DisciplineType.prac,
                "курсов", DisciplineType.cours
        );

        for(var entry: mapBetweenNameAndType.entrySet()){
            if(discipline.getName().toLowerCase().contains(","+entry.getKey())){
                discipline.setName(discipline.getName().replace(","+entry.getKey(), ""));
                discipline.setType(entry.getValue());
                return;
            }
        }
        discipline.setType(DisciplineType.none);
    }

    private String timeByIndex(int index){
        return switch (index){
            case 0 -> "8:00-9:30";
            case 1 -> "9:40-11:10";
            case 2 -> "11:40-13:10";
            case 3 -> "13:30-15:00";
            case 4 -> "15:10-16:40";
            case 5 -> "16:50-18:20";
            case 6 -> "18:30-20:00";
            default -> "-----------";
        };
    }

    private Integer[] res(Integer[] col, Integer[] pairs){
        ArrayList<Integer> res = new ArrayList<>(pairs.length);
        int currentIndex = 0;
        int remainder = col[currentIndex];
        for (int i = 0; i < pairs.length; i++) {
            res.add(currentIndex);
            remainder -= pairs[i];
            if(remainder <= 0 && ++currentIndex < col.length) {
                remainder = col[currentIndex];
            }
        }
        return res.toArray(new Integer[0]);
    }
}
