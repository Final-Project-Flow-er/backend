package com.chaing.domain.users.event;

import com.chaing.core.enums.BucketName;
import org.springframework.web.multipart.MultipartFile;

public record ProfileImageUploadEvent(
        MultipartFile file,
        String fileName,
        BucketName bucketName
) {
}
