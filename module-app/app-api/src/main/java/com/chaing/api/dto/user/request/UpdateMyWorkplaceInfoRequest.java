package com.chaing.api.dto.user.request;

import java.time.LocalTime;

public record UpdateMyWorkplaceInfoRequest(

        String phone,

        // 가맹점 전용
        String operatingDays,
        LocalTime openTime,
        LocalTime closeTime,
        String imageUrl,

        // 공장 전용
        Integer productionLineCount
) {
}
