package com.oexil.staffid.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

@Slf4j
@Component
public class FileUtilizer {

    public boolean writeToDisk(MultipartFile file, Path dir, String fileName) {
        try{
            Path filepath = Paths.get(dir.toString(), fileName);

            try (OutputStream os = Files.newOutputStream(filepath)) {
                os.write(file.getBytes());
            }
            return true;
        }catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String generateFileName(String originalFileName) {
        return new Random().nextInt(100000) + System.currentTimeMillis() + "." + FilenameUtils.getExtension(originalFileName);
    }

    public static void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path); // Delete the file
            }
        } catch (IOException e) {
            throw new RuntimeException("File deletion failed: " + e.getMessage(), e);
        }
    }

}
