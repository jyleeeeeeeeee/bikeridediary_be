package com.bikeridediary.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@Profile("!local")
public class S3ImageStorageService implements ImageStorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public S3ImageStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(MultipartFile file, String folder) throws IOException {
        String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    @Override
    public void delete(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패: {}", e.getMessage());
        }
    }

    @Override
    public boolean isExist(String fileUrl) {
        return false;
    }

    @Override
    public Resource getResource(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        var response = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        return new InputStreamResource(response);
    }



    private String extractKeyFromUrl(String fileUrl) {
        // https://bucket.s3.region.amazonaws.com/folder/file.jpg → folder/file.jpg
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        return fileUrl.replace(prefix, "");
    }
}