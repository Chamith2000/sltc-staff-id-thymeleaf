package com.oexil.staffid.service;

import com.oexil.staffid.utils.FileUtilizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageUploadService {

    @Value("${archive.path}")
    private String archivePath;

    @Value("${server.file.prefix}")
    private String filePrefix;

    public String[] getResultsOfFileWrite(MultipartFile imageFile) {
        String urlPrefix = filePrefix + "/";
        String fileName = new FileUtilizer().generateFileName(imageFile.getOriginalFilename());

        if (!new FileUtilizer().writeToDisk(imageFile, Paths.get(archivePath), fileName)) {
            return null;
        } else {
            return new String[]{(archivePath + "/" + fileName), (urlPrefix + fileName), (fileName)};
        }
    }

    public byte[] readImageFile(String fileName) {
        try {
            // Construct full file path using the archive path
            Path fullPath = Paths.get(archivePath, fileName);

            // Check if file exists
            if (!Files.exists(fullPath)) {
                System.err.println("Image file not found: " + fullPath.toString());
                return null;
            }

            // Read file bytes
            return Files.readAllBytes(fullPath);

        } catch (IOException e) {
            // Log the error
            System.err.println("Error reading image file: " + fileName + " - " + e.getMessage());
            return null;
        }
    }
}