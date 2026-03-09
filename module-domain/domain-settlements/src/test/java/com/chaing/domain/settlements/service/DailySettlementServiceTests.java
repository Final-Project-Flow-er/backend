package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.exception.SettlementException;
import com.chaing.domain.settlements.repository.interfaces.DailyReceiptLineRepository;
import com.chaing.domain.settlements.repository.interfaces.DailySettlementReceiptRepository;
import com.chaing.domain.settlements.service.impl.DailySettlementServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailySettlementServiceTests {
    @Mock
    private DailySettlementReceiptRepository receiptRepository;
    @Mock
    private DailyReceiptLineRepository lineRepository;
    @InjectMocks
    private DailySettlementServiceImpl dailyService;
    @DisplayName("가맹점 ID와 날짜로 일별 정산 영수증 조회 - 성공")
    @Test
    void getByFranchiseAndDate_Success() {
        // given
        Long franchiseId = 1L;
        LocalDate date = LocalDate.of(2026, 3, 9);
        DailySettlementReceipt mockReceipt = DailySettlementReceipt.builder()
                .dailyReceiptId(10L)
                .franchiseId(franchiseId)
                .settlementDate(date)
                .build();

        when(receiptRepository.findByFranchiseIdAndSettlementDate(franchiseId, date))
                .thenReturn(Optional.of(mockReceipt));
        // when
        DailySettlementReceipt result = dailyService.getByFranchiseAndDate(franchiseId, date);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getDailyReceiptId()).isEqualTo(10L);
        verify(receiptRepository).findByFranchiseIdAndSettlementDate(franchiseId, date);
    }
    @DisplayName("일별 정산 영수증 조회 - 실패 (데이터 없음 에러 발생)")
    @Test
    void getByFranchiseAndDate_Fail_NotFound() {
        // given
        Long franchiseId = 1L;
        LocalDate date = LocalDate.of(2026, 3, 9);
        when(receiptRepository.findByFranchiseIdAndSettlementDate(franchiseId, date))
                .thenReturn(Optional.empty());
        // when, then
        assertThatThrownBy(() -> dailyService.getByFranchiseAndDate(franchiseId, date))
                .isInstanceOf(SettlementException.class)
                .hasMessageContaining("해당 일별 정산을 찾을 수 없습니다.");
    }

}