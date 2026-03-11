package com.chaing.core.service;

import com.chaing.core.dto.TargetType;
import com.chaing.core.entity.Image;
import com.chaing.core.enums.BucketName;
import com.chaing.core.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MinioService minioService;

    // 이미지 리스트 조회
    public List<Image> getImagesByTarget(TargetType targetType, Long targetId) {
        return imageRepository.findAllByTargetTypeAndTargetId(targetType, targetId);
    }

    // 파일 저장
    public void saveImages(List<MultipartFile> files, TargetType targetType, Long targetId, BucketName bucket) {
        if (files == null || files.isEmpty()) return;

        for (MultipartFile file : files) {
            String storedName = minioService.generateFileName(file);
            minioService.uploadFile(file, storedName, bucket);
            registerFileRollback(storedName, bucket);

            Image image = Image.builder()
                    .originName(file.getOriginalFilename())
                    .storedName(storedName)
                    .fileSize(file.getSize())
                    .ext(extractExt(file.getOriginalFilename()))
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();

            imageRepository.save(image);
        }
    }

    // 특정 타겟의 모든 이미지 삭제
    public void deleteAllByTarget(TargetType targetType, Long targetId, BucketName bucket) {
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId(targetType, targetId);
        if (images.isEmpty()) return;

        imageRepository.deleteAllByTargetTypeAndTargetId(targetType, targetId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    images.forEach(img -> minioService.deleteFile(img.getStoredName(), bucket));
                }
            }
        });
    }

    // 이름으로 이미지 삭제
    public void deleteByStoredName(String storedName, BucketName bucket) {
        imageRepository.deleteByStoredName(storedName);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_COMMITTED) {
                        minioService.deleteFile(storedName, bucket);
                    }
                }
            });
        }
    }

    // 트랜잭션 롤백 시 MinIO 파일도 삭제
    private void registerFileRollback(String storedName, BucketName bucket) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        minioService.deleteFile(storedName, bucket);
                    }
                }
            });
        }
    }

    private String extractExt(String filename) {
        if (filename == null || !filename.contains("."))
            return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
