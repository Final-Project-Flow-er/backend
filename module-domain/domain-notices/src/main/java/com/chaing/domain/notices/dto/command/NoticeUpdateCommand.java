package com.chaing.domain.notices.dto.command;

public record NoticeUpdateCommand(

        String title,
        String content,
        Boolean important
) {
}
