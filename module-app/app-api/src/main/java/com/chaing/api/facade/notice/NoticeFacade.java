package com.chaing.api.facade.notice;

import com.chaing.api.dto.notice.request.CreateNoticeRequest;
import com.chaing.api.dto.notice.request.UpdateNoticeRequest;
import com.chaing.api.dto.notice.response.NoticeDetailResponse;
import com.chaing.api.dto.notice.response.NoticeSummaryResponse;
import com.chaing.domain.notices.dto.command.NoticeCreateCommand;
import com.chaing.domain.notices.dto.command.NoticeUpdateCommand;
import com.chaing.domain.notices.entity.Notice;
import com.chaing.domain.notices.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeFacade {

    private final NoticeService noticeService;

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
    @Transactional
    public NoticeDetailResponse createNotice(CreateNoticeRequest request, Long authorId) {
        NoticeCreateCommand command = request.toCommand();
        Notice notice = noticeService.create(command, authorId);
        // TODO: 공지사항 알림 생성
        return NoticeDetailResponse.from(notice);
    }

    // 공지사항 수정
    @Transactional
    public NoticeDetailResponse updateNotice(Long id, UpdateNoticeRequest request, Long authorId) {
        NoticeUpdateCommand command = request.toCommand();
        Notice notice = noticeService.update(id, command, authorId);
        // TODO: 공지사항 알림 생성
        return NoticeDetailResponse.from(notice);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long id) {
        noticeService.delete(id);
    }
}
