package com.chaing.api.facade.notice;

import com.chaing.api.dto.notice.request.CreateNoticeRequest;
import com.chaing.api.dto.notice.request.UpdateNoticeRequest;
import com.chaing.api.dto.notice.response.NoticeDetailResponse;
import com.chaing.api.dto.notice.response.NoticeSummaryResponse;
import com.chaing.api.facade.notification.NotificationFacade;
import com.chaing.domain.notices.entity.Notice;
import com.chaing.domain.notices.service.NoticeService;
import com.chaing.domain.notifications.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeFacade {

    private final NoticeService noticeService;
    private final NotificationFacade notificationFacade;

    // 공지사항 상세 조회
    public NoticeDetailResponse getNoticeDetail(Long id) {
        Notice notice = noticeService.getById(id);
        return NoticeDetailResponse.from(notice);
    }

    // 공지사항 목록 조회
    public Page<NoticeSummaryResponse> getNoticeList(Pageable pageable) {
        Page<Notice> notices = noticeService.getNoticeList(pageable);
        return notices.map(NoticeSummaryResponse::from);
    }

    // 공지사항 등록
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public NoticeDetailResponse createNotice(CreateNoticeRequest request, Long authorId) {
        Notice notice = noticeService.create(request.toCommand(), authorId);

        // 공지사항 알림 생성
        notificationFacade.sendNotificationToAll(
                NotificationType.NOTICE,
                "[공지] " + notice.getTitle(),
                notice.getNoticeId()
        );

        return NoticeDetailResponse.from(notice);
    }

    // 공지사항 수정
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public NoticeDetailResponse updateNotice(Long id, UpdateNoticeRequest request, Long updaterId) {
        Notice notice = noticeService.update(id, request.toCommand(), updaterId);

        // 공지사항 알림 수정
        notificationFacade.updateNotificationsByTarget(
                NotificationType.NOTICE,
                "[수정된 공지] " + notice.getTitle(),
                notice.getNoticeId()
        );

        return NoticeDetailResponse.from(notice);
    }

    // 공지사항 삭제
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void deleteNotice(Long id) {
        noticeService.delete(id);
        notificationFacade.deleteNotificationsByTarget(NotificationType.NOTICE, id);
    }
}
