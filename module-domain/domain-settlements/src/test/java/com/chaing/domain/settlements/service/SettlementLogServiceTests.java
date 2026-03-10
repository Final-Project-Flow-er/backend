package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.SettlementLog;
import com.chaing.domain.settlements.enums.SettlementLogType;
import com.chaing.domain.settlements.repository.interfaces.SettlementLogRepository;
import com.chaing.domain.settlements.service.impl.SettlementLogServiceImpl;
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

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementLogServiceTests {

    @Mock
    private SettlementLogRepository repository;

    @InjectMocks
    private SettlementLogServiceImpl logService;

    @DisplayName("정산 로그 생성 - 성공")
    @Test
    void create_Success() {
        // given
        SettlementLog log = SettlementLog.builder()
                .content("테스트 로그 생성")
                .type(SettlementLogType.SETTLEMENT_CONFIRMED)
                .build();
        when(repository.save(log)).thenReturn(log);

        // when
        SettlementLog result = logService.create(log);

        // then
        assertThat(result.getContent()).isEqualTo("테스트 로그 생성");
        verify(repository).save(log);
    }

    @DisplayName("전체 로그 최신순 조회 - 성공")
    @Test
    void getAll_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        SettlementLog mockLog = SettlementLog.builder().build();
        Page<SettlementLog> mockPage = new PageImpl<>(List.of(mockLog));
        when(repository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(mockPage);

        // when
        Page<SettlementLog> result = logService.getAll(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @DisplayName("특정 유형 로그 최신순 조회 - 성공")
    @Test
    void getAllByType_Success() {
        // given
        SettlementLogType type = SettlementLogType.ADJUSTMENT_CREATED;
        Pageable pageable = PageRequest.of(0, 10);
        SettlementLog mockLog = SettlementLog.builder().build();
        Page<SettlementLog> mockPage = new PageImpl<>(List.of(mockLog));
        when(repository.findAllByTypeOrderByCreatedAtDesc(type, pageable)).thenReturn(mockPage);

        // when
        Page<SettlementLog> result = logService.getAllByType(type, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findAllByTypeOrderByCreatedAtDesc(type, pageable);
    }

    @DisplayName("조건(가맹점, 유형)별 로그 내역 조회 - 성공")
    @Test
    void getAllByConditions_Success() {
        // given
        Long franchiseId = 100L;
        SettlementLogType type = SettlementLogType.SETTLEMENT_CONFIRMED;
        Pageable pageable = PageRequest.of(0, 10);
        SettlementLog mockLog = SettlementLog.builder()
                .franchiseId(franchiseId)
                .type(type)
                .build();
        Page<SettlementLog> mockPage = new PageImpl<>(List.of(mockLog));

        when(repository.findByConditions(franchiseId, type, pageable)).thenReturn(mockPage);

        // when
        Page<SettlementLog> result = logService.getAllByConditions(franchiseId, type, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findByConditions(franchiseId, type, pageable);
    }

}
