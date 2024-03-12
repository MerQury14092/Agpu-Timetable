package com.merqury.agpu.rest;

import com.merqury.agpu.DTO.FullArticle;
import com.merqury.agpu.DTO.NewsResponse;
import com.merqury.agpu.service.GetNewsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.internal.StringUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/news")
@Log4j2
public class NewsController {
    private final GetNewsService service;

    @GetMapping
    public NewsResponse getGeneralNews(HttpServletRequest request) throws IOException {
        int page = 1;
        if(request.getParameter("page") != null) {
            String pageStr = request.getParameter("page");
            if(StringUtil.isNumeric(pageStr))
                page = Integer.parseInt(pageStr);

        }
        return service.getAgpuNews(page);
    }

    @GetMapping("/")
    public NewsResponse getGeneralNewsDupl(HttpServletRequest request) throws IOException {
        return getGeneralNews(request);
    }

    @GetMapping("/{faculty}")
    public NewsResponse getArticlesByFaculty(
            @PathVariable String faculty,
            HttpServletRequest request
    ) throws IOException
    {
        int page = 1;
        if(request.getParameter("page") != null) {
            String pageStr = request.getParameter("page");
            if(StringUtil.isNumeric(pageStr))
                page = Integer.parseInt(pageStr);

        }
        return service.getArticlesByFaculty(faculty, page);
    }

    @GetMapping("/{faculty}/")
    public NewsResponse getArticleByFacultyDupl(
            @PathVariable String faculty,
            HttpServletRequest request
    )throws IOException
    {
        return getArticlesByFaculty(faculty, request);
    }

    @GetMapping("/{faculty}/{id}")
    public FullArticle getArticleById(
            @PathVariable String faculty,
            @PathVariable int id
    ) throws IOException
    {
            return service.getArticleById(faculty, id);
    }
}

