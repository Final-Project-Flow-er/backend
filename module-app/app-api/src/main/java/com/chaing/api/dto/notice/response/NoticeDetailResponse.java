package com.chaing.api.dto.notice.response;

import com.chaing.domain.notices.entity.Notice;
import lombok.Builder;

@Builder
public record NoticeDetailResponse(

        String title,
        String content,
        Long authorId,
        boolean important
) {
    public static NoticeDetailResponse from(Notice notice) {
        return new NoticeDetailResponse(
                notice.getTitle(),
                notice.getContent(),
                notice.getAuthorId(),
                notice.isImportant()
        );
    }
}
