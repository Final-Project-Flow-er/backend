package com.chaing.api.dto.notice.request;

import com.chaing.domain.notices.dto.command.NoticeCreateCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateNoticeRequest(

        @NotBlank(message = "공지사항 제목은 필수입니다.")
        String title,

        @NotBlank(message = "공지사항 내용은 필수입니다.")
        String content,

        boolean important
) {
    public NoticeCreateCommand toCommand() {
        return new NoticeCreateCommand(
                this.title,
                this.content,
                this.important
        );
    }
}
