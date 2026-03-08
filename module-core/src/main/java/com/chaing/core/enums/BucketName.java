package com.chaing.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BucketName {

    PROFILES("chaing-profiles");

    private final String bucketName;
}
