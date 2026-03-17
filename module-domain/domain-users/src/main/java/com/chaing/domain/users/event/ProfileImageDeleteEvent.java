package com.chaing.domain.users.event;

import com.chaing.core.enums.BucketName;

public record ProfileImageDeleteEvent(
        String fileName,
        BucketName bucketName
) {
}
