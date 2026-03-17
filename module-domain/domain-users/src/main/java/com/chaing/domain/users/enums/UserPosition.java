package com.chaing.domain.users.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserPosition {

    // 본사
    HR_MANAGER("인사 관리자", UserRole.HQ),
    FINANCE_MANAGER("정산 관리자", UserRole.HQ),
    LOGISTICS_MANAGER("물류 관리자", UserRole.HQ),
    SYSTEM_MANAGER("시스템 관리자", UserRole.HQ),

    // 가맹점
    OWNER("점주", UserRole.FRANCHISE),
    STORE_MANAGER("매니저", UserRole.FRANCHISE),
    STAFF("직원", UserRole.FRANCHISE),

    // 공장
    PRODUCTION_MANAGER("생산 관리자", UserRole.FACTORY),
    FACTORY_LOGISTICS_MANAGER("공장 물류 관리자", UserRole.FACTORY),
    FACTORY_MANAGER("공장 관리자", UserRole.FACTORY);

    private final String description;
    private final UserRole parentRole;

    public boolean isAvailableFor(UserRole role) {
        return this.parentRole == role;
    }
}
