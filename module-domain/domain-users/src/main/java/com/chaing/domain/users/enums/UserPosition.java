package com.chaing.domain.users.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserPosition {

    // 본사
    HR_MANAGER("인사 관리자"),
    FINANCE_MANAGER("정산 관리자"),
    LOGISTICS_MANAGER("물류 관리자"),
    SYSTEM_MANAGER("시스템 관리자"),

    // 가맹점
    OWNER("점주"),
    STORE_MANAGER("매니저"),
    STAFF("직원"),

    // 공장
    PRODUCTION_MANAGER("생산 관리자"),
    FACTORY_LOGISTICS_MANAGER("공장 물류 관리자"),
    FACTORY_MANAGER("공장 관리자");

    private final String description;
}
