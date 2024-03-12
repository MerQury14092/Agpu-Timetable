package com.merqury.agpu.service;

import com.merqury.agpu.DTO.Week;
import com.merqury.agpu.memory.WeekMemory;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
@Log4j2
public class GetWeeksService {

    private final WeekMemory memory;

    @Autowired
    public GetWeeksService(WeekMemory memory) {
        this.memory = memory;
        memory.addAll(parseHtml());
        memory.initWeeks();
    }

    public List<Week> getEverything(){
        return memory.getEverything();
    }

    private List<Week> parseHtml(){
        Scanner sc = new Scanner(GetWeeksService.class.getResourceAsStream("/static/weeks"));

        StringBuilder builder = new StringBuilder();

        while (sc.hasNextLine()){
            builder.append(sc.nextLine()).append("\n");
        }

        Document document = Jsoup.parse(builder.toString());

        List<Week> weeks = new ArrayList<>();

        for(Element el: document.
                getElementsByClass("col-4 mb-3")
        )
            weeks.add(parseWeekend(el));

        return weeks;
    }

    private Week parseWeekend(Element el){ // сюда приходит col-4 mb-3
        Week res = new Week();
        Elements properties = el.getElementsByTag("span");
        res.setId(Integer.parseInt(properties.get(0).text()));
        res.setFrom(properties.get(1).text().replace("с ",""));
        res.setTo(properties.get(2).text().replace("по ",""));
        return res;
    }
}