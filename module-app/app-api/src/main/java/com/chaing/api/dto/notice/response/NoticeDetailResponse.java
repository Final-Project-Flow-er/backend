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
        String updaterName,
        boolean important,
        LocalDateTime importantUntil,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
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

    public static NoticeDetailResponse from(Notice notice, String authorName, String updaterName, Notice prev, Notice next) {
        return new NoticeDetailResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                authorName,
                updaterName,
                notice.isCurrentlyImportant(),
                notice.getImportantUntil(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                NoticeNav.from(prev),
                NoticeNav.from(next));
    }
}
