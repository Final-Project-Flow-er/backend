package com.chaing.domain.returns.service;

import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnInfo;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnItemStatus;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.repository.FranchiseReturnItemRepository;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import com.chaing.domain.returns.repository.interfaces.FranchiseReturnRepositoryCustom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FranchiseReturnServiceTests {
    @InjectMocks
    private FranchiseReturnService franchiseReturnService;

    @Mock
    private FranchiseReturnRepository franchiseReturnRepository;

    @Mock
    private FranchiseReturnItemRepository franchiseReturnItemRepository;

    @Mock
    private FranchiseReturnRepositoryCustom franchiseReturnRepositoryCustom;

    Long franchiseId;
    String username;
    String phoneNumber;

    String returnCode;
    Long orderId;
    Integer quantity;
    LocalDateTime requestedDate;
    Long orderItemId;
    String description;

    BigDecimal unitPrice;

    FranchiseReturnAndReturnItemResponse franchiseReturnAndReturnItemResponse;

    Returns returns;
    Long returnId;
    ReturnItem returnItem;
    Long returnItemId;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;

        username = "username";
        phoneNumber = "phoneNumber";

        returnCode = "ReturnCode";
        returnId = 2L;
        description = "description";
        unitPrice = BigDecimal.TEN;

        orderId = 10L;
        quantity = 10;
        requestedDate = LocalDateTime.now();
        orderItemId = 100L;

        franchiseReturnAndReturnItemResponse = FranchiseReturnAndReturnItemResponse.builder()
                .returnCode(returnCode)
                .status(ReturnStatus.PENDING)
                .franchiseOrderId(orderId)
                .quantity(quantity)
                .type(ReturnType.MISORDER)
                .requestedDate(requestedDate)
                .franchiseOrderItemId(orderItemId)
                .build();

        returns = Returns.builder()
                .franchiseId(franchiseId)
                .franchiseOrderId(orderId)
                .returnCode(returnCode)
                .username(username)
                .phoneNumber(phoneNumber)
                .returnType(ReturnType.MISORDER)
                .description(description)
                .totalReturnQuantity(quantity)
                .totalReturnAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .returnStatus(ReturnStatus.PENDING)
                .build();
        ReflectionTestUtils.setField(returns, "returnId", returnId);

        returnItem = ReturnItem.builder()
                .returns(returns)
                .franchiseOrderItemId(orderItemId)
                .quantity(quantity)
                .isInspected(false)
                .returnItemStatus(ReturnItemStatus.BEFORE_INSPECTION)
                .build();
        ReflectionTestUtils.setField(returnItem, "returnItemId", orderId);
    }

    @Test
    @DisplayName("모든 반품 기록 조회 - 성공")
    void getAllReturns_Success() {
        // given
        given(franchiseReturnRepositoryCustom.searchAllReturns(franchiseId)).willReturn(List.of(franchiseReturnAndReturnItemResponse));

        // when
        List<FranchiseReturnAndReturnItemResponse> responses = franchiseReturnService.getAllReturns(franchiseId);

        // then
        assertEquals(returnCode, responses.get(0).returnCode());
        assertEquals(orderId, responses.get(0).franchiseOrderId());
        assertEquals(requestedDate, responses.get(0).requestedDate());
        assertEquals(orderItemId, responses.get(0).franchiseOrderItemId());
    }

    @Test
    @DisplayName("특정 반품 세부사항 조회 - 성공")
    void getReturn_Success() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)).willReturn(Optional.of(returns));

        // when
        FranchiseReturnInfo response = franchiseReturnService.getReturn(username, franchiseId, returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode);
        assertEquals(returnCode, response.returnCode());
    }

    @Test
    @DisplayName("특정 반품 조회 시 반품 데이터 없을 때 예외 발생")
    void getReturn_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.getReturn(username, franchiseId, returnCode);
        });
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("orderItemId 반환")
    void getAllReturnItemOrderItemId_Success() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCode(returnCode)).willReturn(List.of(returnItem));

        // when
        List<Long> response = franchiseReturnService.getAllReturnItemOrderItemId(returnCode);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnCode(returnCode);
        assertEquals(orderItemId, response.get(0));
    }
}