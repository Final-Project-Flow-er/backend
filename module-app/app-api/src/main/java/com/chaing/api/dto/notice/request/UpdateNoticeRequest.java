package com.chaing.api.dto.notice.request;

import com.chaing.domain.notices.dto.command.NoticeUpdateCommand;
import java.time.LocalDateTime;
import java.util.List;

public record UpdateNoticeRequest(

        String title,
        String content,
        Boolean important,
        LocalDateTime importantUntil,
        List<String> deleteStoredFileNames
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
