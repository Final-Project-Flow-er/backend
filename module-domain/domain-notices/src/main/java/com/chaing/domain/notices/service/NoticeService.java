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
        return noticeRepository.findAllByOrderByImportantDescCreatedAtDesc(pageable);
    }

    // 공지사항 등록
    public Notice create(NoticeCreateCommand command, Long authorId) {
        Notice notice = Notice.createNotice(command, authorId);
        return noticeRepository.save(notice);
    }

    // 공지사항 수정
    public Notice update(Long id, NoticeUpdateCommand command, Long updaterId) {
        Notice notice = getById(id);
        notice.updateNotice(command, updaterId);
        return notice;
    }


    // 공지사항 삭제 - 본사
    public void delete(Long id) {
        Notice notice = getById(id);
        notice.delete();
    }
}
