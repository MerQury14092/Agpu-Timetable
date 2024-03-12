package com.merqury.agpu.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FullArticle extends Article{
    List<String> images;

    public FullArticle(){
        images = new ArrayList<>();
    }
}
