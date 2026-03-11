package com.chaing.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BucketName {

    PROFILES("chaing-profiles"),
    SETTLEMENTS("chaing-settlements"),
    NOTICES("chaing-notices");

    private final String bucketName;
}
