package com.chaing.domain.users.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    ADMIN("총 관리자"),
    HQ("본사"),
    FRANCHISE("가맹점"),
    FACTORY("공장");

    private final String description;
}
