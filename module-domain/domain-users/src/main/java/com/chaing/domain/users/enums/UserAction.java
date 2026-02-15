package com.chaing.domain.users.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserAction {

    REGISTER("신규 등록"),
    UPDATE("정보 수정"),
    DEACTIVATE("계정 비활성화"),
    RESTORE("계정 복구"),
    DELETE("계정 삭제");

    private final String description;
}
