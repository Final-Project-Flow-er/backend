package com.chaing.api.dto.notice.response;

import com.chaing.domain.notices.entity.Notice;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeDetailResponse(

        Long id,
        String title,
        String content,
        String authorName,
        boolean important,
        LocalDateTime importantUntil,
        LocalDateTime createdAt,
        NoticeNav prevNotice,
        NoticeNav nextNotice
) {
    public record NoticeNav(Long id, String title) {
        public static NoticeNav from(Notice notice) {
            if (notice == null)
                return null;
            return new NoticeNav(notice.getNoticeId(), notice.getTitle());
        }
    }

    public static NoticeDetailResponse from(Notice notice, String authorName, Notice prev, Notice next) {
        return new NoticeDetailResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                authorName,
                notice.isCurrentlyImportant(),
                notice.getImportantUntil(),
                notice.getCreatedAt(),
                NoticeNav.from(prev),
                NoticeNav.from(next));
    }
}
