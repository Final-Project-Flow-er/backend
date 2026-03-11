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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTests {

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
        ReflectionTestUtils.setField(notice, "noticeId", noticeId);
        ReflectionTestUtils.setField(notice, "createdAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("공지사항 등록")
    void create() {

        // given
        NoticeCreateCommand command = new NoticeCreateCommand("신규 공지", "내용", true, null);
        given(noticeRepository.findAllEffectiveImportantWithLock(any(LocalDateTime.now().getClass()))).willReturn(new ArrayList<>());
        given(noticeRepository.save(any(Notice.class))).willReturn(notice);

        // when
        Notice result = noticeService.create(command, authorId);

        // then
        assertThat(result).isNotNull();
        verify(noticeRepository).findAllEffectiveImportantWithLock(any(LocalDateTime.now().getClass()));
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    @DisplayName("공지사항 목록 조회")
    void getNoticeList() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        Notice importantNotice = Notice.builder()
                .title("중요 공지")
                .important(true)
                .build();

        Page<Notice> noticePage = new PageImpl<>(List.of(importantNotice, notice), pageable, 2);
        given(noticeRepository.findAllSorted(any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(noticePage);

        // when
        Page<Notice> result = noticeService.getNoticeList(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).isImportant()).isTrue();
        verify(noticeRepository).findAllSorted(any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    @DisplayName("이전글 조회")
    void getPreviousNotice() {

        // given
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));
        Notice prevNotice = Notice.builder().title("이전글").build();

        int expectedIsImportant = notice.isCurrentlyImportant() ? 1 : 0;
        LocalDateTime expectedCreatedAt = notice.getCreatedAt();

        given(noticeRepository.findPreviousNotice(any(LocalDateTime.class), eq(expectedIsImportant), eq(expectedCreatedAt))).willReturn(prevNotice);

        // when
        Notice result = noticeService.getPreviousNotice(noticeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("이전글");
        verify(noticeRepository).findPreviousNotice(any(LocalDateTime.class), eq(expectedIsImportant), eq(expectedCreatedAt));
    }

    @Test
    @DisplayName("다음글 조회")
    void getNextNotice() {

        // given
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));
        Notice nextNotice = Notice.builder().title("다음글").build();

        int expectedIsImportant = notice.isCurrentlyImportant() ? 1 : 0;
        LocalDateTime expectedCreatedAt = notice.getCreatedAt();

        given(noticeRepository.findNextNotice(any(LocalDateTime.class), eq(expectedIsImportant), eq(expectedCreatedAt))).willReturn(nextNotice);

        // when
        Notice result = noticeService.getNextNotice(noticeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("다음글");
        verify(noticeRepository).findNextNotice(any(LocalDateTime.class), eq(expectedIsImportant), eq(expectedCreatedAt));
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
        Long updaterId = 2L;
        NoticeUpdateCommand command = new NoticeUpdateCommand("수정된 제목", "수정된 내용", true, null);
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

        List<Notice> fullList = new ArrayList<>();
        for(long i=1; i<=5; i++) {
            Notice n = Notice.builder().build();
            ReflectionTestUtils.setField(n, "noticeId", i);
            fullList.add(n);
        }
        given(noticeRepository.findAllEffectiveImportantWithLock(any())).willReturn(fullList);

        // when
        Notice result = noticeService.update(noticeId, command, updaterId);

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
        assertThat(notice.getDeletedAt()).isNotNull();
    }
}