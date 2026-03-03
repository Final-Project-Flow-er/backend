package com.chaing.api.dto.hq.user.request;

import com.chaing.domain.users.dto.command.UserUpdateCommand;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;

import java.time.LocalDate;

public record UpdateUserRequest(

        String username,
        String email,
        String phone,
        LocalDate birthDate,
        String profileImageUrl,
        UserRole role,
        UserPosition position,
        Long businessUnitId
) {
    public UserUpdateCommand toCommand() {
        return new UserUpdateCommand(username, email, phone, birthDate, profileImageUrl, role, position, businessUnitId);
    }
}
