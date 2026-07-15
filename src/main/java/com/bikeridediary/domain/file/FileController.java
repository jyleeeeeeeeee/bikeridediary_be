package com.bikeridediary.domain.file;

import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.config.FileStorageProperties;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import com.bikeridediary.utils.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final ImageStorageService imageStorageService;
    private final FileStorageProperties fileStorageProperties;

    @GetMapping("/files/{userId}/{fileName}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String userId,
            @PathVariable String fileName,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        if (!userDetails.getUserId().toString().equals(userId)) {
            throw new BusinessException(ErrorCode.FILE_ACCESS_DENIED);
        }

        String fileUrl = fileStorageProperties.baseUrl() + "/" + userId + "/" + fileName;
        Resource resource = imageStorageService.getResource(fileUrl);

        if(!resource.exists() || !resource.isReadable()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        MediaType mediaType = resolveMediaType(resource);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(resource);
    }

    // 파일 확장자 기반 Content-Type 추론
    private MediaType resolveMediaType(Resource resource) {
        try {
            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType != null) {
                return MediaType.parseMediaType(contentType);
            }
        } catch (IOException ignored) {
            // 추론 실패 시 기본값
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
