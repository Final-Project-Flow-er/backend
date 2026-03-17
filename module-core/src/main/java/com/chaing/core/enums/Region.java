package com.chaing.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Region {

    SEOUL("서울", "SE"),
    GYEONGGI("경기", "GG"),
    INCHEON("인천", "IC"),
    BUSAN("부산", "BS"),
    DAEGU("대구", "DG"),
    DAEJEON("대전", "DJ"),
    GWANGJU("광주", "GJ"),
    ULSAN("울산", "US"),
    SEJONG("세종", "SJ"),
    GANGWON("강원", "GW"),
    CHUNGBUK("충북", "CB"),
    CHUNGNAM("충남", "CN"),
    JEONBUK("전북", "JB"),
    JEONNAM("전남", "JN"),
    GYEONGBUK("경북", "GB"),
    GYEONGNAM("경남", "GN"),
    JEJU("제주", "JJ");

    private final String description;
    private final String code;
}
