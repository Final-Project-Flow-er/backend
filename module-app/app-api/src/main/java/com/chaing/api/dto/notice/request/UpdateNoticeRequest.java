package com.chaing.api.dto.notice.request;

import com.chaing.domain.notices.dto.command.NoticeUpdateCommand;
import java.time.LocalDateTime;

public record UpdateNoticeRequest(

        String title,
        String content,
        Boolean important,
        LocalDateTime importantUntil
) {
    public NoticeUpdateCommand toCommand() {
        return new NoticeUpdateCommand(
                title,
                content,
                important,
                importantUntil
        );
    }
}
