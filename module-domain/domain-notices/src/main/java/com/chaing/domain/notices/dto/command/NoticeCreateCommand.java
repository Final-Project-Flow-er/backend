package com.chaing.domain.notices.dto.command;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record NoticeCreateCommand(

        @NotBlank(message = "공지사항 제목은 필수입니다.")
        String title,

        @NotBlank(message = "공지사항 내용은 필수입니다.")
        String content,

        boolean important,

        LocalDateTime importantUntil
) {
}
