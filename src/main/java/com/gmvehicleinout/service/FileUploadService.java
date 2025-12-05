package com.gmvehicleinout.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
@Service
public class FileUploadService {

    private final String UPLOAD_DIR = "/app/uploads/";

    public String saveFile(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);

            Files.write(path, file.getBytes());

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // RETURN FULL URL FOR FLUTTER
    public String getFileUrl(String fileName) {
        if (fileName == null) return null;
        return "https://gm-vehicle-in-out-java-production.up.railway.app/files/" + fileName;
    }
}
