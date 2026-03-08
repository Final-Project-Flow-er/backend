package com.chaing.core.service;

import com.chaing.core.enums.BucketName;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    // 파일명 생성
    public String generateFileName(MultipartFile file) {
        return UUID.randomUUID() + "_" + file.getOriginalFilename();
    }

    // 파일 업로드 로직
    public void uploadFile(MultipartFile file, String fileName, BucketName bucket) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket.getBucketName())
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO upload error: ", e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    // 이미지 조회 URL 생성
    public String getFileUrl(String fileName, BucketName bucket) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket.getBucketName())
                            .object(fileName)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO get URL error: ", e);
            return null;
        }
    }

    // 이미지 삭제
    public void deleteFile(String fileName, BucketName bucket) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket.getBucketName())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO delete error: ", e);
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.");
        }
    }
}
