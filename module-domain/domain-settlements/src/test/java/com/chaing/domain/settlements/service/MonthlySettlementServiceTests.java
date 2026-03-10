package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.chaing.domain.settlements.enums.SettlementStatus;
import com.chaing.domain.settlements.repository.interfaces.MonthlySettlementRepository;
import com.chaing.domain.settlements.service.impl.MonthlySettlementServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlySettlementServiceTests {

    @Mock
    private MonthlySettlementRepository repository;
    @InjectMocks
    private MonthlySettlementServiceImpl monthlyService;

    @DisplayName("월별 정산 확정 요청 상태로 변경 (CALCULATED -> CONFIRM_REQUESTED)")
    @Test
    void requestConfirm_Success() {
        // given
        Long id = 1L;
        MonthlySettlement settlement = MonthlySettlement.builder()
                .monthlySettlementId(id)
                .status(SettlementStatus.CALCULATED)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(settlement));

        when(repository.save(any(MonthlySettlement.class))).thenReturn(settlement);

        // when
        MonthlySettlement result = monthlyService.requestConfirm(id);
        // then
        assertThat(result.getStatus()).isEqualTo(SettlementStatus.CONFIRM_REQUESTED);
        verify(repository).save(settlement);
    }

    @DisplayName("월별 정산 목록 조회 (전체 가맹점) - 성공")
    @Test
    void getAllByMonth_Success() {
        // given
        YearMonth month = YearMonth.of(2026, 3);
        MonthlySettlement mockSettlement = MonthlySettlement.builder()
                .monthlySettlementId(1L)
                .settlementMonth(month)
                .build();
        when(repository.findAllBySettlementMonth(month)).thenReturn(List.of(mockSettlement));

        // when
        List<MonthlySettlement> result = monthlyService.getAllByMonth(month, null);

        // then
        assertThat(result.size()).isEqualTo(1);
        verify(repository).findAllBySettlementMonth(month);
    }

    @DisplayName("월별 정산 목록 조회 (키워드 검색) - 성공")
    @Test
    void getAllByMonth_WithKeyword_Success() {
        // given
        YearMonth month = YearMonth.of(2026, 3);
        String keyword = "강남";
        MonthlySettlement mockSettlement = MonthlySettlement.builder()
                .monthlySettlementId(1L)
                .settlementMonth(month)
                .build();
        when(repository.findAllBySettlementMonthAndFranchiseNameContaining(month, keyword))
                .thenReturn(List.of(mockSettlement));

        // when
        List<MonthlySettlement> result = monthlyService.getAllByMonth(month, keyword);

        // then
        assertThat(result.size()).isEqualTo(1);
        verify(repository).findAllBySettlementMonthAndFranchiseNameContaining(month, keyword);
    }

    @DisplayName("가맹점 ID와 월로 정산 내역 단건 조회 - 성공")
    @Test
    void getByFranchiseAndMonth_Success() {
        // given
        Long franchiseId = 10L;
        YearMonth month = YearMonth.of(2026, 3);
        MonthlySettlement mockSettlement = MonthlySettlement.builder()
                .monthlySettlementId(1L)
                .build();
        when(repository.findByFranchiseIdAndSettlementMonth(franchiseId, month))
                .thenReturn(Optional.of(mockSettlement));

        // when
        MonthlySettlement result = monthlyService.getByFranchiseAndMonth(franchiseId, month);

        // then
        assertThat(result.getMonthlySettlementId()).isEqualTo(1L);
        verify(repository).findByFranchiseIdAndSettlementMonth(franchiseId, month);
    }

    @DisplayName("가맹점 ID와 월로 정산 내역 단건 조회 - 실패 (데이터 없음)")
    @Test
    void getByFranchiseAndMonth_Fail_NotFound() {
        // given
        Long franchiseId = 10L;
        YearMonth month = YearMonth.of(2026, 3);
        when(repository.findByFranchiseIdAndSettlementMonth(franchiseId, month)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> monthlyService.getByFranchiseAndMonth(franchiseId, month))
                .isInstanceOf(com.chaing.domain.settlements.exception.SettlementException.class)
                .hasMessageContaining("해당 월별 정산을 찾을 수 없습니다.");
    }

    @DisplayName("월별 정산 상태별 카운트 조회 - 성공")
    @Test
    void getStatusCounts_Success() {
        // given
        YearMonth month = YearMonth.of(2026, 3);
        when(repository.countBySettlementMonthAndStatus(month, SettlementStatus.CALCULATED)).thenReturn(10L);
        when(repository.countBySettlementMonthAndStatus(month, SettlementStatus.CONFIRM_REQUESTED)).thenReturn(5L);
        when(repository.countBySettlementMonthAndStatus(month, SettlementStatus.CONFIRMED)).thenReturn(45L);

        // when
        Map<String, Long> result = monthlyService.getStatusCounts(month);

        // then
        assertThat(result.get("CALCULATED")).isEqualTo(10L);
        assertThat(result.get("CONFIRM_REQUESTED")).isEqualTo(5L);
        assertThat(result.get("CONFIRMED")).isEqualTo(45L);

        verify(repository).countBySettlementMonthAndStatus(month, SettlementStatus.CALCULATED);
        verify(repository).countBySettlementMonthAndStatus(month, SettlementStatus.CONFIRM_REQUESTED);
        verify(repository).countBySettlementMonthAndStatus(month, SettlementStatus.CONFIRMED);
    }

}