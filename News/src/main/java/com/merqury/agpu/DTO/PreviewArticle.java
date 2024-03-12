package com.merqury.agpu.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PreviewArticle extends Article{
    private String previewImage;
}