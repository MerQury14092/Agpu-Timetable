package com.merqury.agpu.memory;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ImageUrlMemory {
    private final Map<String, Path> fileLocationsByUrl;

    public ImageUrlMemory() {
        fileLocationsByUrl = new HashMap<>();
    }

    public void addUrl(String url, Path path) {
        fileLocationsByUrl.put(url, path);
        new Thread(() -> {
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                fileLocationsByUrl.remove(url);
                delete(path);
            }

        }).start();
    }

    public Path getPathByUrl(String url){
        return fileLocationsByUrl.get(url);
    }

    private void delete(Path path){
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
