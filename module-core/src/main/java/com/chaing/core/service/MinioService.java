package com.chaing.core.service;

import com.chaing.core.config.MinioConfig;
import com.chaing.core.enums.BucketName;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    // 파일명 생성
    public String generateFileName(MultipartFile file) {
        return UUID.randomUUID() + "_" + file.getOriginalFilename();
    }

    // 파일 업로드 로직
    public void uploadFile(MultipartFile file, String fileName, BucketName bucket) {
        try {
            ensureBucketExists(bucket.getBucketName());
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket.getBucketName())
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (Exception e) {
            log.error("MinIO upload error: ", e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    // byte[] 기반 파일 업로드 로직 (정산 파일용)
    public void uploadFile(byte[] bytes, String fileName, String contentType, BucketName bucket) {
        try {
            ensureBucketExists(bucket.getBucketName());
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket.getBucketName())
                            .object(fileName)
                            .stream(bais, bytes.length, -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            log.error("MinIO byte[] upload error: ", e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    private void ensureBucketExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            log.info("Creating non-existent MinIO bucket: {}", bucketName);
            try {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
            } catch (io.minio.errors.ErrorResponseException e) {
                if (!"BucketAlreadyOwnedByYou".equals(e.errorResponse().code()) &&
                        !"BucketAlreadyExists".equals(e.errorResponse().code())) {
                    throw e;
                }
            }
        }
    }

    // 파일 존재 여부 확인 (MinIO stat)
    public boolean objectExists(String fileName, BucketName bucket) {
        try {
            minioClient.statObject(
                    io.minio.StatObjectArgs.builder()
                            .bucket(bucket.getBucketName())
                            .object(fileName)
                            .build());
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            log.error("MinIO statObject error for key '{}': {}", fileName, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("MinIO objectExists check failed for key '{}': {}", fileName, e.getMessage());
            return false;
        }
    }

    // 이미지 조회 URL 생성
    public String getFileUrl(String fileName, BucketName bucket) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        try {
            String externalUrl = minioConfig.getExternalUrl();

            if (!externalUrl.endsWith("/")) {
                externalUrl += "/";
            }

            return externalUrl + bucket.getBucketName() + "/" + fileName;
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
                            .build());
        } catch (Exception e) {
            log.error("MinIO delete error: ", e);
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.");
        }
    }
}
