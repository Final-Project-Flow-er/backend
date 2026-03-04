package com.chaing.domain.notifications.dto.command;

import com.chaing.domain.notifications.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record NotificationCreateCommand(

        @NotBlank(message = "알림 수신자는 필수입니다.")
        Long userId,

        @NotBlank(message = "알림 타입은 필수입니다.")
        NotificationType type,

        @NotBlank(message = "알림 메시지는 필수입니다.")
        String message,

        Long targetId
) {
}
