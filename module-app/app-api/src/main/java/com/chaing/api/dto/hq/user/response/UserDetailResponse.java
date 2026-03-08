package com.chaing.api.dto.hq.user.response;

import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserDetailResponse(

        String loginId,
        String username,
        String email,
        String phone,
        LocalDate birthDate,
        String employeeNumber,
        String profileImageUrl,
        UserRole role,
        UserPosition position,
        UserStatus status,
        Long businessUnitId
) {
    public static UserDetailResponse from(User user, String profileImageUrl) {

        Long businessUnitId = switch (user.getRole()) {
            case HQ -> user.getHqId();
            case FRANCHISE -> user.getFranchiseId();
            case FACTORY -> user.getFactoryId();
            default -> null;
        };

        return UserDetailResponse.builder()
                .loginId(user.getLoginId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .employeeNumber(user.getEmployeeNumber())
                .profileImageUrl(profileImageUrl)
                .role(user.getRole())
                .position(user.getPosition())
                .status(user.getStatus())
                .businessUnitId(businessUnitId)
                .build();
    }
}
