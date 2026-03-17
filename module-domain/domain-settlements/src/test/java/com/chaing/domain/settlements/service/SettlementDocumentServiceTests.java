package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.SettlementDocument;
import com.chaing.domain.settlements.repository.interfaces.SettlementDocumentRepository;
import com.chaing.domain.settlements.service.impl.SettlementDocumentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementDocumentServiceTests {

        @Mock
        private SettlementDocumentRepository documentRepository;
        @InjectMocks
        private SettlementDocumentServiceImpl documentService;

        @DisplayName("일별 영수증 단건 조회 - 성공")
        @Test
        void getDailyDocument_Success() {
                // given
                Long dailyReceiptId = 1L;
                SettlementDocument mockDocument = SettlementDocument.builder()
                                .fileUrl("https://dummy-url.com/receipt.pdf")
                                .build();

                when(documentRepository.findByDailyReceiptId(dailyReceiptId))
                                .thenReturn(Optional.of(mockDocument));
                // when
                Optional<SettlementDocument> result = documentService.getDailyDocument(dailyReceiptId);
                // then
                assertThat(result.isPresent()).isTrue();
                assertThat(result.get().getFileUrl()).isEqualTo("https://dummy-url.com/receipt.pdf");
        }

        @DisplayName("일별 영수증 단건 조회 - 데이터 없음")
        @Test
        void getDailyDocument_NotFound() {
                // given
                Long dailyReceiptId = 999L;
                when(documentRepository.findByDailyReceiptId(dailyReceiptId))
                                .thenReturn(Optional.empty());
                // when
                Optional<SettlementDocument> result = documentService.getDailyDocument(dailyReceiptId);
                // then
                assertThat(result.isPresent()).isFalse();
        }

        @DisplayName("월별 정산 영수증 목록 조회 - 성공")
        @Test
        void getMonthlyDocuments_Success() {
                // given
                Long monthlySettlementId = 100L;
                SettlementDocument mockDocument = SettlementDocument.builder()
                                .fileUrl("https://dummy-url.com/monthly_receipt.pdf")
                                .build();

                when(documentRepository.findAllByMonthlySettlementId(monthlySettlementId))
                                .thenReturn(List.of(mockDocument));

                // when
                List<SettlementDocument> result = documentService.getMonthlyDocuments(monthlySettlementId);

                // then
                assertThat(result.size()).isEqualTo(1);
                assertThat(result.get(0).getFileUrl()).isEqualTo("https://dummy-url.com/monthly_receipt.pdf");
                verify(documentRepository).findAllByMonthlySettlementId(monthlySettlementId);
        }
}