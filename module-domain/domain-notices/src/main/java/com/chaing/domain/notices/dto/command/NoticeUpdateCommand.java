package com.chaing.domain.notices.dto.command;

import java.time.LocalDateTime;

public record NoticeUpdateCommand(

                String title,
                String content,
                Boolean important,
                LocalDateTime importantUntil
) {
}
