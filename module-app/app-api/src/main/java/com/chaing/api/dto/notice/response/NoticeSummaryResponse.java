package com.chaing.api.dto.notice.response;

import com.chaing.domain.notices.entity.Notice;
import lombok.Builder;

@Builder
public record NoticeSummaryResponse(

        String title,
        Long authorId,
        boolean important
) {
    public static NoticeSummaryResponse from(Notice notice) {
        return new NoticeSummaryResponse(
                notice.getTitle(),
                notice.getAuthorId(),
                notice.isImportant()
        );
    }
}
