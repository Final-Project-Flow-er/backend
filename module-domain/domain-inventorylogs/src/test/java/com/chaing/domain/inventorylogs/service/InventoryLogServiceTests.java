package com.chaing.domain.inventorylogs.service;

import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.repository.InventoryLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class InventoryLogServiceTests {

        @Mock
        private InventoryLogRepository inventoryLogRepository;

        @InjectMocks
        private InventoryLogService inventoryLogService;

        private LogRequest logRequest;
        private FranchiseLogRequest franchiseLogRequest;
        private FactoryLogRequest factoryLogRequest;

        @BeforeEach
        void setUp() {
                logRequest = mock(LogRequest.class);
                franchiseLogRequest = mock(FranchiseLogRequest.class);
                factoryLogRequest = mock(FactoryLogRequest.class);
        }

        @DisplayName("반품입고")
        @Test
        void findReturnInboundLogs() {
                // given
                InventoryLogListResponse response = mock(InventoryLogListResponse.class);
                when(inventoryLogRepository.findReturnInboundLogs(any(), any(Pageable.class)))
                                .thenReturn(response);

                // when
                InventoryLogListResponse result = inventoryLogService.findReturnInboundLogs(logRequest,
                                mock(Pageable.class));

                // then
                assertSame(response, result);
        }

        @DisplayName("반품 출고")
        @Test
        void findReturnOutboundLogs() {
                InventoryLogListResponse response = mock(InventoryLogListResponse.class);
                when(inventoryLogRepository.findReturnOutboundLogs(any(), any(Pageable.class)))
                                .thenReturn(response);

                InventoryLogListResponse result = inventoryLogService.findReturnOutboundLogs(logRequest,
                                mock(Pageable.class));

                assertSame(response, result);
        }

        @DisplayName("폐기")
        @Test
        void findDisposalLogs() {
                InventoryLogListResponse response = mock(InventoryLogListResponse.class);
                when(inventoryLogRepository.findDisposalLogs(any(), any(Pageable.class)))
                                .thenReturn(response);

                InventoryLogListResponse result = inventoryLogService.findDisposalLogs(logRequest,
                                mock(Pageable.class));

                assertSame(response, result);
        }

        @DisplayName("가맹점 입출고")
        @Test
        void findFranchiseInboundOutboundLogs() {
                Long franchiseId = 1L;
                FranchiseInventoryLogListResponse response = mock(FranchiseInventoryLogListResponse.class);

                when(inventoryLogRepository.findFranchiseInboundOutboundLogs(any(), any(), any(Pageable.class)))
                                .thenReturn(response);

                FranchiseInventoryLogListResponse result = inventoryLogService.findFranchiseInboundOutboundLogs(
                                franchiseId, franchiseLogRequest, mock(Pageable.class));

                assertSame(response, result);
        }

        @DisplayName("가맹점 판매, 환불")
        @Test
        void findFranchiseSalesRefundLogs() {
                Long franchiseId = 1L;
                FranchiseInventoryLogListResponse response = mock(FranchiseInventoryLogListResponse.class);

                when(inventoryLogRepository.findFranchiseSalesRefundLogs(any(), any(), any(Pageable.class)))
                                .thenReturn(response);

                FranchiseInventoryLogListResponse result = inventoryLogService.findFranchiseSalesRefundLogs(franchiseId,
                                franchiseLogRequest, mock(Pageable.class));

                assertSame(response, result);
        }

        @DisplayName("공장 재고 이력 조회")
        @Test
        void findFactoryInventoryLogs() {
                Long factoryId = 1L;
                FactoryInventoryLogListResponse response = mock(FactoryInventoryLogListResponse.class);

                when(inventoryLogRepository.findFactoryInventoryLogs(any(), any(), any(Pageable.class)))
                                .thenReturn(response);

                FactoryInventoryLogListResponse result = inventoryLogService.findFactoryInventoryLogs(factoryId,
                                factoryLogRequest, mock(Pageable.class));

                assertSame(response, result);
        }
}