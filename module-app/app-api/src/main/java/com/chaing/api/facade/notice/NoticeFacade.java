package com.chaing.api.facade.notice;

import com.chaing.api.dto.notice.request.CreateNoticeRequest;
import com.chaing.api.dto.notice.request.UpdateNoticeRequest;
import com.chaing.api.dto.notice.response.NoticeDetailResponse;
import com.chaing.api.dto.notice.response.NoticeSummaryResponse;
import com.chaing.core.dto.TargetType;
import com.chaing.core.entity.Image;
import com.chaing.core.enums.BucketName;
import com.chaing.core.service.ImageService;
import com.chaing.core.service.MinioService;
import com.chaing.domain.notifications.event.NotificationEvent;
import com.chaing.api.facade.notification.NotificationFacade;
import com.chaing.domain.notices.entity.Notice;
import com.chaing.domain.notices.service.NoticeService;
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeFacade {

    private final NoticeService noticeService;
    private final NotificationFacade notificationFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final UserManagementService userManagementService;
    private final MinioService minioService;
    private final ImageService imageService;

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "svg");

    // 공지사항 상세 조회
    public NoticeDetailResponse getNoticeDetail(Long id) {
        Notice notice = noticeService.getById(id);
        String authorName = getName(notice.getAuthorId());
        String updaterName = getName(notice.getUpdaterId());

        Notice prev = noticeService.getPreviousNotice(id);
        Notice next = noticeService.getNextNotice(id);

        List<Image> files = imageService.getImagesByTarget(TargetType.NOTICE, id);
        List<NoticeDetailResponse.FileInfo> images = new ArrayList<>();
        List<NoticeDetailResponse.FileInfo> attachments = new ArrayList<>();

        for (Image file : files) {
            String url = minioService.getFileUrl(file.getStoredName(), BucketName.NOTICES);
            NoticeDetailResponse.FileInfo info = new NoticeDetailResponse.FileInfo(
                    file.getOriginName(),
                    file.getStoredName(),
                    url,
                    file.getFileSize());

            if (IMAGE_EXTENSIONS.contains(file.getExt().toLowerCase())) {
                images.add(info);
            } else {
                attachments.add(info);
            }
        }

        return NoticeDetailResponse.from(notice, authorName, updaterName, prev, next, images, attachments);
    }

    // 공지사항 목록 조회
    public Page<NoticeSummaryResponse> getNoticeList(Pageable pageable) {
        Page<Notice> notices = noticeService.getNoticeList(pageable);
        return notices.map(notice -> NoticeSummaryResponse.from(notice, getName(notice.getAuthorId())));
    }

    // 공지사항 등록
    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailResponse createNotice(CreateNoticeRequest request, List<MultipartFile> images, Long authorId) {
        Notice notice = noticeService.create(request.toCommand(), authorId);

        if (images != null && !images.isEmpty()) {
            imageService.saveImages(images, TargetType.NOTICE, notice.getNoticeId(), BucketName.NOTICES);
        }

        eventPublisher.publishEvent(NotificationEvent.ofAll(
                NotificationType.NOTICE,
                "[공지] " + notice.getTitle(),
                notice.getNoticeId()
        ));

        String authorName = getName(authorId);
        return NoticeDetailResponse.from(notice, authorName, null, null, null, null, null);
    }

    // 공지사항 수정
    @Transactional(rollbackFor = Exception.class)
    public NoticeDetailResponse updateNotice(Long id, UpdateNoticeRequest request, List<MultipartFile> images,
            Long updaterId) {
        Notice notice = noticeService.update(id, request.toCommand(), updaterId);

        if (request.deleteStoredFileNames() != null && !request.deleteStoredFileNames().isEmpty()) {
            for (String storedName : request.deleteStoredFileNames()) {
                imageService.deleteByStoredName(storedName, BucketName.NOTICES);
            }
        }

        if (images != null && !images.isEmpty()) {
            imageService.saveImages(images, TargetType.NOTICE, id, BucketName.NOTICES);
        }

        eventPublisher.publishEvent(NotificationEvent.ofUpdate(
                NotificationType.NOTICE,
                "[수정된 공지] " + notice.getTitle(),
                notice.getNoticeId()
        ));

        return NoticeDetailResponse.from(notice, getName(notice.getAuthorId()), getName(updaterId), null, null, null, null);
    }

    // 사용자 이름 조회
    private String getName(Long userId) {
        if (userId == null) return null;

        try {
            User user = userManagementService.getUserById(userId);
            return user.getUsername();
        } catch (Exception e) {
            return "알 수 없음";
        }
    }

    // 공지사항 삭제
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        noticeService.delete(id);
        imageService.deleteAllByTarget(TargetType.NOTICE, id, BucketName.NOTICES);
        notificationFacade.deleteNotificationsByTarget(NotificationType.NOTICE, id);
    }
}
