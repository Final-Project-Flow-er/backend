package com.chaing.domain.notices.service;

import com.chaing.domain.notices.dto.command.NoticeCreateCommand;
import com.chaing.domain.notices.dto.command.NoticeUpdateCommand;
import com.chaing.domain.notices.entity.Notice;
import com.chaing.domain.notices.exception.NoticeErrorCode;
import com.chaing.domain.notices.exception.NoticeException;
import com.chaing.domain.notices.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // 공지사항 상세 조회
    public Notice getById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    // 공지사항 목록 조회
    public Page<Notice> getNoticeList(Pageable pageable) {
        return noticeRepository.findAllSorted(LocalDateTime.now(), pageable);
    }

    // 공지사항 등록
    public Notice create(NoticeCreateCommand command, Long authorId) {
        if (command.important()) {
            validateImportantLimit(command.importantUntil());
        }
        Notice notice = Notice.createNotice(command, authorId);
        return noticeRepository.save(notice);
    }

    // 공지사항 수정
    public Notice update(Long id, NoticeUpdateCommand command, Long updaterId) {
        Notice notice = getById(id);

        if (command.important() != null && command.important()) {
            validateImportantLimit(command.importantUntil());
        }

        notice.updateNotice(command, updaterId);
        return notice;
    }

    // 이전글
    public Notice getPreviousNotice(Long currentId) {
        Notice current = getById(currentId);
        int isImportant = current.isCurrentlyImportant() ? 1 : 0;
        return noticeRepository.findPreviousNotice(LocalDateTime.now(), isImportant, current.getCreatedAt());
    }

    // 다음글
    public Notice getNextNotice(Long currentId) {
        Notice current = getById(currentId);
        int isImportant = current.isCurrentlyImportant() ? 1 : 0;
        return noticeRepository.findNextNotice(LocalDateTime.now(), isImportant, current.getCreatedAt());
    }

    // 공지사항 삭제
    public void delete(Long id) {
        Notice notice = getById(id);
        notice.delete();
    }

    // 중요 공지 마감일 검증
    private void validateImportantLimit(LocalDateTime importantUntil) {
        LocalDateTime now = LocalDateTime.now();
        if (importantUntil != null && importantUntil.isBefore(now)) return;

        List<Notice> importantNotices = noticeRepository.findAllEffectiveImportantWithLock(now);
        if (importantNotices.size() >= 5) {
            throw new NoticeException(NoticeErrorCode.TOO_MANY_IMPORTANT_NOTICES);
        }
    }
}
