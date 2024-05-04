package com.merqury.agpu.rest;

import com.merqury.agpu.memory.ImageUrlMemory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping("/api/${api.version}/timetable/images")
public class ImageGetController {
    private final ImageUrlMemory imageUrlMemory;

    public ImageGetController(ImageUrlMemory memory) {
        this.imageUrlMemory = memory;
    }

    @GetMapping(value = "/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getImage(@PathVariable String id) throws MalformedURLException {
        Path path = imageUrlMemory.getPathByUrl(id);
        if(Files.notExists(path))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(new UrlResource(path.toUri()));
    }
}
