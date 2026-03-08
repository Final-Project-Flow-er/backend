package com.chaing.api.dto.user.event;

import com.chaing.core.enums.BucketName;
import org.springframework.web.multipart.MultipartFile;

public record ProfileImageUploadEvent(
        MultipartFile file,
        String fileName,
        BucketName bucketName
) {
}
