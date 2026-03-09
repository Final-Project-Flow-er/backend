package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.SettlementAdjustment;
import com.chaing.domain.settlements.repository.interfaces.SettlementAdjustmentRepository;
import com.chaing.domain.settlements.service.impl.SettlementAdjustmentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementAdjustmentServiceTests {

    @Mock
    private SettlementAdjustmentRepository repository;
    @InjectMocks
    private SettlementAdjustmentServiceImpl adjustmentService;
    @DisplayName("새로운 조정 내역 생성(저장)")
    @Test
    void createAdjustment_Success() {
        // given
        SettlementAdjustment adjustment = SettlementAdjustment.builder()
                .adjustmentAmount(new BigDecimal("5000"))
                .reason("배송 지연 보상")
                .build();

        when(repository.save(adjustment)).thenReturn(adjustment);
        // when
        SettlementAdjustment result = adjustmentService.create(adjustment);
        // then
        assertThat(result.getAdjustmentAmount()).isEqualTo(new BigDecimal("5000"));
        assertThat(result.getReason()).isEqualTo("배송 지연 보상");
        verify(repository).save(adjustment);
    }

}