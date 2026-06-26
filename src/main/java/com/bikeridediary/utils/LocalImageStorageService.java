package com.bikeridediary.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@Profile("local")
@RequiredArgsConstructor
public class LocalImageStorageService implements ImageStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8081/files}")
    private String baseUrl;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Path.of(uploadDir));
    }

    @Override
    public String upload(MultipartFile file, String folder) throws IOException {
        String fileName = createFileName(file);
        Path dirPath = Path.of(uploadDir, folder);
        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(fileName);
        file.transferTo(filePath);
        String uniqueName = folder + "/" + fileName;

        return baseUrl + "/" + uniqueName;
    }

    @Override
    public void delete(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(baseUrl + "/", "");
            Files.deleteIfExists(Path.of(uploadDir, relativePath));
        } catch (IOException e) {
            log.warn("로컬 파일 삭제 실패: {}", e.getMessage());
        }
    }

    @Override
    public boolean isExist(String fileUrl) {
        return Files.exists(Path.of(fileUrl.replace(baseUrl + "/", "")));
    }

    @Override
    public Resource getResource(String fileUrl) {
        String relativePath = fileUrl.replace(baseUrl + "/", "");
        Path filePath = Path.of(uploadDir, relativePath);
        return new FileSystemResource(filePath);
    }


    private static String createFileName(MultipartFile file) {
        return UUID.randomUUID() + "_" + file.getOriginalFilename();
    }

}
