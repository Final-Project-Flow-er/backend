package com.chaing.api.dto.user.event;

import com.chaing.core.enums.BucketName;

public record ProfileImageDeleteEvent(
        String fileName,
        BucketName bucketName
) {
}
