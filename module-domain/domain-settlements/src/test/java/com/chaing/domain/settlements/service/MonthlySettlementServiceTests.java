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

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

}