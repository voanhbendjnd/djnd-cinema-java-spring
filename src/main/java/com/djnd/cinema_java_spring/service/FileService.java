package com.djnd.cinema_java_spring.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.nio.file.attribute.BasicFileAttributes;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FileService {
    @Value("${djnd.upload-file.base-uri}")
    private String absolutePathURLServer;
    public static final String moviePoster = "movie-poster";
    public static final String movieTemp = "movie-temps";

    public String getNameFileAtTemp(MultipartFile file) throws URISyntaxException, IOException {
        var uploadPath = absolutePathURLServer + movieTemp;
        var directoryPath = Paths.get(uploadPath);
        Files.createDirectories(directoryPath);
        var originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IOException("File name not found!");
        }
        var fileNameSave = "djnd" + "-" + System.currentTimeMillis() + ".webp";
        var filePath = directoryPath.resolve(fileNameSave);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileNameSave;
    }

    public String moveSaveFromTempToOther(String fileName, String to) throws URISyntaxException, IOException {
        var tempPath = Paths.get(absolutePathURLServer + movieTemp).resolve(fileName);
        var saveTo = Paths.get(absolutePathURLServer + to);
        Files.createDirectories(saveTo);
        var lastPath = saveTo.resolve(fileName);
        if (Files.exists(tempPath)) {
            // move
            Files.move(tempPath, lastPath, StandardCopyOption.REPLACE_EXISTING);
            return Paths.get(to).resolve(fileName).toString().replace("\\", "/");
        } else {
            throw new IOException("Temp file does not exist: " + tempPath);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void clearOldTempAfterDay() {
        var directoryPath = Paths.get(absolutePathURLServer + movieTemp);
        if (!Files.exists(directoryPath)) {
            return;
        }
        long twentyFourHoursAgo = Instant.now().minusSeconds(24 * 60 * 60).toEpochMilli();
        try {
            Files.list(directoryPath).forEach(file -> {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    if (attrs.creationTime().toMillis() <= twentyFourHoursAgo) {
                        Files.delete(file);
                        System.out.println("Deleted old temp file: " + file.getFileName());
                    }
                } catch (IOException e) {
                    System.err.println("Error processing file in " + movieTemp + file.getFileName());
                }
            });
        } catch (IOException e) {
            System.err.println("Error listing " + movieTemp + " directory.");
        }
    }

}
