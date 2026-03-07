package com.chaing.core.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucketName;

    // 파일 업로드 로직
    public String uploadFile(MultipartFile file, String targetBucket) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return fileName;
        } catch (Exception e) {
            log.error("MinIO upload error: ", e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    // 이미지 조회 URL 생성
    public String getFileUrl(String fileName, String targetBucket) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(targetBucket)
                            .object(fileName)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO get URL error: ", e);
            return null;
        }
    }
}
