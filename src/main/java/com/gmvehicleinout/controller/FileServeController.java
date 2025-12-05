package com.gmvehicleinout.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/files")
public class FileServeController {

    private final String UPLOAD_DIR = "/tmp/uploads/";

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            byte[] fileBytes = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", Files.probeContentType(filePath));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}



