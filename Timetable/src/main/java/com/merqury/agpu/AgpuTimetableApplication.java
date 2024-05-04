package com.merqury.agpu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@SpringBootApplication
public class AgpuTimetableApplication {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Path imageStorageDir;
            if(System.getenv("SAVE_PATH") == null)
                imageStorageDir = Path.of("/var/timetable-service/images");
            else
                imageStorageDir = Path.of(System.getenv("SAVE_PATH"));
            try {
                Files.walkFileTree(imageStorageDir, new SimpleFileVisitor<>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        SpringApplication.run(AgpuTimetableApplication.class, args);
    }
}
