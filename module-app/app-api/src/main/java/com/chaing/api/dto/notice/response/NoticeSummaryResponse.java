package com.chaing.api.dto.notice.response;

import com.chaing.domain.notices.entity.Notice;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeSummaryResponse(

        Long id,
        String title,
        String authorName,
        boolean important,
        LocalDateTime importantUntil,
        LocalDateTime createdAt
) {
    public static NoticeSummaryResponse from(Notice notice, String authorName) {
        return new NoticeSummaryResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                authorName,
                notice.isCurrentlyImportant(),
                notice.getImportantUntil(),
                notice.getCreatedAt());
    }
}
