package com.chaing.api.dto.notice.request;

import com.chaing.domain.notices.dto.command.NoticeUpdateCommand;

public record UpdateNoticeRequest(

        String title,
        String content,
        boolean important
) {
    public NoticeUpdateCommand toCommand() {
        return new NoticeUpdateCommand(
                this.title,
                this.content,
                this.important
        );
    }
}
