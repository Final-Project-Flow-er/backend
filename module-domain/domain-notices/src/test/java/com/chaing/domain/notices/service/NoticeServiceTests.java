package com.chaing.domain.notices.service;

import com.chaing.domain.notices.dto.command.NoticeCreateCommand;
import com.chaing.domain.notices.dto.command.NoticeUpdateCommand;
import com.chaing.domain.notices.entity.Notice;
import com.chaing.domain.notices.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock
    private NoticeRepository noticeRepository;

    private Notice notice;
    private final Long authorId = 1L;
    private final Long noticeId = 1L;

    @BeforeEach
    void setUp() {
        notice = Notice.builder()
                .title("제목")
                .content("내용")
                .important(false)
                .authorId(authorId)
                .build();
    }

    @Test
    @DisplayName("공지사항 등록")
    void create() {

        // given
        NoticeCreateCommand command = new NoticeCreateCommand("신규 공지", "내용", true);
        given(noticeRepository.save(any(Notice.class))).willReturn(notice);

        // when
        Notice result = noticeService.create(command, authorId);

        // then
        assertThat(result).isNotNull();
        verify(noticeRepository, times(1)).save(any(Notice.class));
    }

    @Test
    @DisplayName("공지사항 상세 조회")
    void getById() {

        // given
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

        // when
        Notice result = noticeService.getById(noticeId);

        // then
        assertThat(result.getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("공지사항 수정")
    void update() {

        // given
        NoticeUpdateCommand command = new NoticeUpdateCommand("수정된 제목", "수정된 내용", true);
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

        // when
        Notice result = noticeService.update(noticeId, command, authorId);

        // then
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.isImportant()).isTrue();
    }

    @Test
    @DisplayName("공지사항 삭제")
    void delete() {

        // given
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

        // when
        noticeService.delete(noticeId);

        // then
        verify(noticeRepository, times(1)).findById(noticeId);
    }
}