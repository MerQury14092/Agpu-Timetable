package com.merqury.agpu.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NewsResponse {
    private int currentPage;
    private int countPages;
    private List<PreviewArticle> articles;
    public NewsResponse(){
        articles = new ArrayList<>();
    }
}
