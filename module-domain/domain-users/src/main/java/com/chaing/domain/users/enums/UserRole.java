package com.chaing.domain.users.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    HQ("본사"),
    FRANCHISE("가맹점"),
    FACTORY("공장");

    private final String description;
}
