package com.bikeridediary.utils;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorageService {
    String upload(MultipartFile file, String folder) throws IOException;
    void delete(String fileUrl);

    boolean isExist(String fileUrl);
    Resource getResource(String fileUrl);
}
