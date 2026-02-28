package com.chaing.domain.users.dto.command;

import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;

import java.time.LocalDate;

public record UserUpdateCommand(

        String username,
        String email,
        String phone,
        LocalDate birthDate,
        String profileImageUrl,
        UserRole role,
        UserPosition position,
        Long businessUnitId
) {
}
