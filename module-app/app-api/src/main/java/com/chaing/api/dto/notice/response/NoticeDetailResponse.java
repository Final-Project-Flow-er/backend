package com.chaing.api.dto.notice.response;

import com.chaing.domain.notices.entity.Notice;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

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
        NoticeNav nextNotice,
        List<String> imageUrls
) {
    public record NoticeNav(Long id, String title) {
        public static NoticeNav from(Notice notice) {
            if (notice == null) return null;
            return new NoticeNav(notice.getNoticeId(), notice.getTitle());
        }
    }

    public static NoticeDetailResponse from(Notice notice, String authorName, String updaterName, Notice prev, Notice next, List<String> imageUrls) {
        return NoticeDetailResponse.builder()
                .id(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .authorName(authorName)
                .updaterName(updaterName)
                .important(notice.isCurrentlyImportant())
                .importantUntil(notice.getImportantUntil())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .prevNotice(NoticeNav.from(prev))
                .nextNotice(NoticeNav.from(next))
                .imageUrls(imageUrls) // [추가]
                .build();
    }
}
