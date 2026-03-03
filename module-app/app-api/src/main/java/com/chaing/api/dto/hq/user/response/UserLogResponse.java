package com.chaing.api.dto.hq.user.response;

import com.chaing.domain.users.entity.UserLog;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.enums.UserPosition;
import com.chaing.domain.users.enums.UserRole;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record UserLogResponse(

        Long logId,
        Long targetUserId,
        Long actorId,
        UserAction action,
        String targetUsername,
        String employeeNumber,
        String email,
        String phone,
        LocalDate birthDate,
        UserRole role,
        UserPosition position,
        LocalDateTime createdAt
) {
    public static UserLogResponse from(UserLog log) {
        return UserLogResponse.builder()
                .logId(log.getLogId())
                .targetUserId(log.getTargetUserId())
                .actorId(log.getActorId())
                .action(log.getAction())
                .targetUsername(log.getTargetUsername())
                .employeeNumber(log.getEmployeeNumber())
                .email(log.getEmail())
                .phone(log.getPhone())
                .birthDate(log.getBirthDate())
                .role(log.getRole())
                .position(log.getPosition())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
