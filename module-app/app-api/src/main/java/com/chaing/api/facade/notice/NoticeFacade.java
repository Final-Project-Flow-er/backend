package com.chaing.api.facade.notice;

import com.chaing.api.dto.notice.request.CreateNoticeRequest;
import com.chaing.api.dto.notice.request.UpdateNoticeRequest;
import com.chaing.api.dto.notice.response.NoticeDetailResponse;
import com.chaing.api.dto.notice.response.NoticeSummaryResponse;
import com.chaing.domain.notifications.event.NotificationEvent;
import com.chaing.api.facade.notification.NotificationFacade;
import com.chaing.domain.notices.entity.Notice;
import com.chaing.domain.notices.service.NoticeService;
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    // 공지사항 상세 조회
    public NoticeDetailResponse getNoticeDetail(Long id) {
        Notice notice = noticeService.getById(id);
        String authorName = getName(notice.getAuthorId());
        String updaterName = getName(notice.getUpdaterId());
        Notice prev = noticeService.getPreviousNotice(id);
        Notice next = noticeService.getNextNotice(id);

        return NoticeDetailResponse.from(notice, authorName, updaterName, prev, next);
    }

    // 공지사항 목록 조회
    public Page<NoticeSummaryResponse> getNoticeList(Pageable pageable) {
        Page<Notice> notices = noticeService.getNoticeList(pageable);
        return notices.map(notice -> {
            String authorName = getName(notice.getAuthorId());
            return NoticeSummaryResponse.from(notice, authorName);
        });
    }

    // 공지사항 등록
    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailResponse createNotice(CreateNoticeRequest request, Long authorId) {
        Notice notice = noticeService.create(request.toCommand(), authorId);

        eventPublisher.publishEvent(NotificationEvent.ofAll(
                NotificationType.NOTICE,
                "[공지] " + notice.getTitle(),
                notice.getNoticeId()
        ));

        String authorName = getName(authorId);
        return NoticeDetailResponse.from(notice, authorName, null, null, null);
    }

    // 공지사항 수정
    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailResponse updateNotice(Long id, UpdateNoticeRequest request, Long updaterId) {
        Notice notice = noticeService.update(id, request.toCommand(), updaterId);

        eventPublisher.publishEvent(NotificationEvent.ofUpdate(
                NotificationType.NOTICE,
                "[수정된 공지] " + notice.getTitle(),
                notice.getNoticeId()
        ));

        String authorName = getName(notice.getAuthorId());
        String updaterName = getName(notice.getUpdaterId());
        return NoticeDetailResponse.from(notice, authorName, updaterName, null, null);
    }

    // 사용자 이름 조회
    private String getName(Long userId) {
        if (userId == null)
            return null;
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("알 수 없음");
    }

    // 공지사항 삭제
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void deleteNotice(Long id) {
        noticeService.delete(id);
        notificationFacade.deleteNotificationsByTarget(NotificationType.NOTICE, id);
    }
}
