package com.chaing.api.dto.user.response;

import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MyInfoResponse(

        String loginId,
        String username,
        String email,
        String phone,
        LocalDate birthDate,
        String employeeNumber,
        String profileImageUrl,
        UserRole role,
        UserPosition position
) {
    public static MyInfoResponse from(User user, String profileImageUrl) {

        return MyInfoResponse.builder()
                .loginId(user.getLoginId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .employeeNumber(user.getEmployeeNumber())
                .profileImageUrl(profileImageUrl)
                .role(user.getRole())
                .position(user.getPosition())
                .build();
    }
}
