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
import java.util.List;
import java.util.Optional;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.enums.VoucherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
                                .hasMessageContaining("해당 날짜의 정산 내역이 존재하지 않습니다.");
        }

        @DisplayName("날짜와 특정 가맹점 키워드로 일별 정산 목록 조회 - 성공")
        @Test
        void getAllByDate_WithKeyword_Success() {
                // given
                LocalDate date = LocalDate.of(2026, 3, 10);
                String keyword = "강남";
                DailySettlementReceipt mockReceipt = DailySettlementReceipt.builder()
                                .dailyReceiptId(1L)
                                .settlementDate(date)
                                .build();
                when(receiptRepository.findAllBySettlementDateAndFranchiseNameContaining(date, keyword))
                                .thenReturn(List.of(mockReceipt));

                // when
                List<DailySettlementReceipt> result = dailyService.getAllByDate(date, keyword);

                // then
                assertThat(result.size()).isEqualTo(1);
                verify(receiptRepository).findAllBySettlementDateAndFranchiseNameContaining(date, keyword);
        }

        @DisplayName("기간별 정산 내역 조회 (그래프용) - 성공")
        @Test
        void getAllByDateRange_Success() {
                // given
                LocalDate start = LocalDate.of(2026, 3, 1);
                LocalDate end = LocalDate.of(2026, 3, 10);
                DailySettlementReceipt mockReceipt = DailySettlementReceipt.builder()
                                .dailyReceiptId(1L)
                                .build();
                when(receiptRepository.findAllBySettlementDateBetween(start, end))
                                .thenReturn(List.of(mockReceipt));

                // when
                List<DailySettlementReceipt> result = dailyService.getAllByDateRange(start, end);

                // then
                assertThat(result.size()).isEqualTo(1);
                verify(receiptRepository).findAllBySettlementDateBetween(start, end);
        }

        @DisplayName("영수증 상세 내역 유형별 조회 (페이징) - 성공")
        @Test
        void getReceiptLines_WithType_Success() {
                // given
                Long receiptId = 10L;
                VoucherType type = VoucherType.LOSS;
                Pageable pageable = PageRequest.of(0, 20);

                DailyReceiptLine mockLine = DailyReceiptLine.builder()
                                .lineType(type)
                                .build();
                Page<DailyReceiptLine> mockPage = new PageImpl<>(List.of(mockLine));

                when(lineRepository.findAllByDailyReceiptIdAndLineType(receiptId, type, pageable))
                                .thenReturn(mockPage);

                // when
                Page<DailyReceiptLine> result = dailyService.getReceiptLines(receiptId, type, pageable);

                // then
                assertThat(result.getTotalElements()).isEqualTo(1);
                verify(lineRepository).findAllByDailyReceiptIdAndLineType(receiptId, type, pageable);
        }

}