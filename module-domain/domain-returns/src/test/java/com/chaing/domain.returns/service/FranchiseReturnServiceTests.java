package com.chaing.domain.returns.service;

import com.chaing.core.dto.returns.response.FranchiseOrderInfo;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.command.HQReturnDetailCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCreateCommand;
import com.chaing.domain.returns.dto.command.ReturnItemInspection;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.FranchiseReturnItemCreateRequest;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnInfo;
import com.chaing.domain.returns.dto.response.FranchiseReturnProductInfo;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.dto.response.ReturnAndOrderInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private ReturnCodeGenerator generator;

    Long franchiseId;
    String franchiseCode;
    String username;
    String phoneNumber;

    String returnCode;
    Long orderId;
    Integer quantity;
    LocalDateTime requestedDate;
    Long orderItemId;
    String orderCode;
    String description;

    BigDecimal unitPrice;

    String boxCode;
    String serialCode;
    String productCode;
    String productName;

    FranchiseReturnAndReturnItemResponse franchiseReturnAndReturnItemResponse;
    FranchiseReturnCreateRequest franchiseReturnCreateRequest;
    FranchiseReturnItemCreateRequest franchiseReturnItemCreateRequest;
    FranchiseOrderInfo franchiseOrderInfo;
    ReturnAndOrderInfo returnAndOrderInfo;
    ReturnItemCreateCommand returnItemCreateCommand;
    FranchiseReturnProductInfo franchiseReturnProductInfo;

    Returns returns;
    Long returnId;
    ReturnItem returnItem;
    Long returnItemId;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;
        franchiseCode = "FranchiseCode";

        username = "username";
        phoneNumber = "phoneNumber";

        returnCode = "ReturnCode";
        returnId = 2L;
        description = "description";
        unitPrice = BigDecimal.TEN;
        returnItemId = 3L;

        orderId = 10L;
        orderCode = "orderCode";
        quantity = 10;
        requestedDate = LocalDateTime.now();
        orderItemId = 100L;

        boxCode = "BoxCode";
        serialCode = "serialCode";
        productCode = "ProductCode";
        productName = "ProductName";

        franchiseReturnAndReturnItemResponse = FranchiseReturnAndReturnItemResponse.builder()
                .returnCode(returnCode)
                .status(ReturnStatus.PENDING)
                .franchiseOrderId(orderId)
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
                .isInspected(false)
                .returnItemStatus(ReturnItemStatus.BEFORE_INSPECTION)
                .build();
        ReflectionTestUtils.setField(returnItem, "returnItemId", returnItemId);

        franchiseReturnItemCreateRequest = new FranchiseReturnItemCreateRequest(
                boxCode,
                productCode,
                productName,
                unitPrice
        );

        franchiseReturnCreateRequest = new FranchiseReturnCreateRequest(
                orderCode,
                ReturnType.MISORDER,
                description,
                unitPrice.multiply(BigDecimal.valueOf(quantity)),
                List.of(franchiseReturnItemCreateRequest)
        );

        franchiseOrderInfo = FranchiseOrderInfo.builder()
                .orderId(orderId)
                .username(username)
                .phoneNumber(phoneNumber)
                .franchiseCode(franchiseCode)
                .build();

        returnAndOrderInfo = new ReturnAndOrderInfo(
                orderItemId,
                returnItemId
        );

        returnItemCreateCommand = new ReturnItemCreateCommand(
                serialCode,
                orderItemId
        );

        franchiseReturnProductInfo = new FranchiseReturnProductInfo(
                boxCode,
                serialCode,
                productCode,
                productName,
                unitPrice
        );
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

    @Test
    @DisplayName("반품 제품 수정 - 성공")
    void updateReturnItems_Success() {
        // given
        given(franchiseReturnRepository.findByReturnCode(returnCode)).willReturn(Optional.of(returns));
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCode(returnCode)).willReturn(List.of(returnItem));
        Map<String, Long> orderItemIds = new HashMap<>();
        orderItemIds.put(serialCode, orderItemId);

        // when
        List<FranchiseReturnProductInfo> responses = franchiseReturnService.updateReturnItems(List.of(franchiseReturnProductInfo), returnCode, orderItemIds);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnCode(returnCode);
        verify(franchiseReturnRepository, times(1)).findByReturnCode(returnCode);
        assertEquals(boxCode, responses.get(0).boxCode());
        assertEquals(serialCode, responses.get(0).serialCode());
        assertEquals(productCode, responses.get(0).productCode());
        assertEquals(productName, responses.get(0).productName());
        assertEquals(unitPrice, responses.get(0).unitPrice());
    }

    @Test
    @DisplayName("잘못된 반품 코드로 반품 조회 시 예외 발생")
    void updateReturnItems_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByReturnCode(returnCode)).willReturn(Optional.empty());
        Map<String, Long> orderItemIds = new HashMap<>();
        orderItemIds.put(serialCode, orderItemId);

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.updateReturnItems(List.of(franchiseReturnProductInfo), returnCode, orderItemIds);
        });
        verify(franchiseReturnRepository, times(1)).findByReturnCode(returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 조회 - 성공")
    void getReturnInfo_Success() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)).willReturn(Optional.of(returns));

        // when
        ReturnInfo response = franchiseReturnService.getReturnInfo(username, franchiseId, returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode);
        assertEquals(returnCode, response.returnCode());
    }

    @Test
    @DisplayName("존재하지 않는 returnCode로 조회 시 예외 발생")
    void getReturnInfo_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.getReturnInfo(username, franchiseId, returnCode);
        });
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("returnCode로 반품 취소 - 성공")
    void cancelReturn_Success() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)).willReturn(Optional.of(returns));

        // when
        String response = franchiseReturnService.cancel(franchiseId, username, returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode);
        assertEquals(returnCode, response);
    }

    @Test
    @DisplayName("존재하지 않는 returnCode로 반품 조회 시 예외 발생")
    void cancelReturn_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.cancel(franchiseId, username, returnCode);
        });
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("요청사항에 대한 반품 생성 - 성공")
    void createReturn_Success() {
        // given
        when(generator.generate()).thenReturn("RET-0001");

        // when
        ReturnInfo response = franchiseReturnService.createReturn(franchiseId, franchiseReturnCreateRequest, franchiseOrderInfo);

        // then
        assertEquals("RET-0001", response.returnCode());
        assertEquals(ReturnStatus.PENDING, response.status());
        assertEquals(orderId, response.franchiseOrderId());
        assertEquals(ReturnType.PRODUCT_DEFECT, response.type());
    }

    @Test
    @DisplayName("요청사항에 대한 반품 제품 생성 - 성공")
    void createReturnItem_Success() {
        // given
        given(franchiseReturnRepository.findByReturnCode(returnCode)).willReturn(Optional.of(returns));

        // when
        List<ReturnAndOrderInfo> responses = franchiseReturnService.createReturnItems(returnCode, List.of(returnItemCreateCommand));

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnCode(returnCode);
        assertEquals(orderItemId, responses.get(0).orderItemId());
    }

    @Test
    @DisplayName("존재하지 않는 반품 코드로 반품 조회 시 예외 발생")
    void createReturnItem_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByReturnCode(returnCode)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.createReturnItems(returnCode, List.of(returnItemCreateCommand));
        });
        verify(franchiseReturnRepository, times(1)).findByReturnCode(returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대기 상태의 반품 요청 조회 - 성공")
    void getAllReturnByStatus_Success() {
        // given
        given(franchiseReturnRepository.findAllByReturnStatus(ReturnStatus.PENDING)).willReturn(List.of(returns));

        // when
        Map<Long, HQReturnCommand> response = franchiseReturnService.getAllReturnByStatus(ReturnStatus.PENDING);

        // then
        verify(franchiseReturnRepository, times(1)).findAllByReturnStatus(ReturnStatus.PENDING);
        assertEquals(ReturnStatus.PENDING, response.values().stream().findFirst().get().status());
    }

    @Test
    @DisplayName("대기 상태의 반품 제품 조회 - 성공")
    void getAllReturnItemByStatus_Success() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnStatus(ReturnStatus.PENDING)).willReturn(List.of(returnItem));

        // when
        Map<Long, List<ReturnAndOrderInfo>> response = franchiseReturnService.getAllReturnItemByStatus(ReturnStatus.PENDING);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnStatus(ReturnStatus.PENDING);
        assertEquals(returnItemId, response.values().stream().findFirst().get().get(0).returnItemId());
        assertEquals(orderItemId, response.values().stream().findFirst().get().get(0).orderItemId());
    }

    @Test
    @DisplayName("반품에 대한 반품 제품이 없을 시 예외 발생")
    void getAllReturnItemByStatus_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnStatus(ReturnStatus.PENDING)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.getAllReturnItemByStatus(ReturnStatus.PENDING);
        });
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnStatus(ReturnStatus.PENDING);
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대기 상태가 아닌 반품 요청 조회 - 성공")
    void getAllNotPendingReturn_Success() {
        // given
        Returns notPending = Returns.builder().returnStatus(ReturnStatus.SHIPPING).build();
        given(franchiseReturnRepository.findAllByReturnStatusNot(ReturnStatus.PENDING)).willReturn(List.of(notPending));

        // when
        Map<Long, HQReturnCommand>  response = franchiseReturnService.getAllNotPendingReturn();

        // then
        verify(franchiseReturnRepository, times(1)).findAllByReturnStatusNot(ReturnStatus.PENDING);
        assertEquals(ReturnStatus.SHIPPING, response.values().stream().findFirst().get().status());
    }

    @Test
    @DisplayName("대기 상태가 아닌 반품 제품 조회 - 성공")
    void getAllNotPendingReturnItem_Success() {
        // given
        Long notPendingId = 3L;
        Returns notPending = Returns.builder()
                .returnStatus(ReturnStatus.SHIPPING)
                .build();
        ReflectionTestUtils.setField(notPending, "returnId", notPendingId);

        Long returnItemId = 4L;
        ReturnItem item = ReturnItem.builder()
                .returns(notPending)
                .franchiseOrderItemId(orderItemId)
                .build();
        ReflectionTestUtils.setField(item, "returnItemId", returnItemId);
        given(franchiseReturnItemRepository.findAllByReturns_ReturnStatusNot(ReturnStatus.PENDING)).willReturn(List.of(item));

        // when
        Map<Long, List<ReturnAndOrderInfo>> response = franchiseReturnService.getAllNotPendingReturnItem();

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnStatusNot(ReturnStatus.PENDING);
        assertEquals(returnItemId, response.values().stream().findFirst().get().get(0).returnItemId());
        assertEquals(orderItemId, response.values().stream().findFirst().get().get(0).orderItemId());
    }

    @Test
    @DisplayName("반품에 대한 반품 제품이 없을 시 예외 발생")
    void getAllNotPendingReturnItem_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnStatusNot(ReturnStatus.PENDING)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.getAllNotPendingReturnItem();
        });
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnStatusNot(ReturnStatus.PENDING);
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("본사 특정 반품 조회 - 성공")
    void getHQReturnInfo() {
        // given
        given(franchiseReturnRepository.findByReturnCode(returnCode)).willReturn(Optional.of(returns));

        // when
        HQReturnDetailCommand response = franchiseReturnService.getHQReturnInfo(returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnCode(returnCode);
        assertEquals(returnId, response.returnId());
        assertEquals(orderId, response.franchiseOrderId());
        assertEquals(phoneNumber, response.phoneNumber());
    }

    @Test
    @DisplayName("잘못된 값으로 반품 조회 시 예외 발생")
    void getHQReturnInfo_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByReturnCode(returnCode)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.getHQReturnInfo(returnCode);
        });
        verify(franchiseReturnRepository, times(1)).findByReturnCode(returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품에 해당하는 반품 제품 id 조회")
    void getReturnItemId_Success() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCode(returnCode)).willReturn(List.of(returnItem));

        // when
        Map<Long, Long> response = franchiseReturnService.getReturnItemId(returnCode);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnCode(returnCode);
        assertEquals(returnItemId, response.keySet().stream().findFirst().get());
        assertEquals(orderItemId, response.values().stream().findFirst().get());
    }

    @Test
    @DisplayName("반품에 해당하는 제품이 없을 시 예외 발생")
    void getReturnItemId_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCode(returnCode)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.getReturnItemId(returnCode);
        });
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnCode(returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 제품 검수 상태 반환 - 성공")
    void getReturnItemInspection_Success() {
        // given
        given(franchiseReturnItemRepository.findAllByReturnItemIdIn(List.of(returnItemId))).willReturn(List.of(returnItem));

        // when
        Map<Long, ReturnItemInspection> response = franchiseReturnService.getReturnItemInspection(List.of(returnItemId));

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturnItemIdIn(List.of(returnItemId));
        assertEquals(returnItemId, response.keySet().stream().findFirst().get());
        assertEquals(false, response.values().stream().findFirst().get().isInspected());
        assertEquals(ReturnItemStatus.BEFORE_INSPECTION, response.values().stream().findFirst().get().status());
    }

    @Test
    @DisplayName("잘못된 값으로 반품 제품 조회 시 예외 발생")
    void getReturnItemInspection_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findAllByReturnItemIdIn(List.of(returnItemId))).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.getReturnItemInspection(List.of(returnItemId));
        });
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 제품 검수 상태 업데이트 - 성공")
    void updateReturnItemStatus_Success() {
        // given
        given(franchiseReturnItemRepository.findAllByReturnItemIdIn(List.of(returnItemId))).willReturn(List.of(returnItem));

        Map<Long, String> serialCodeByReturnItemId = Map.of(returnItemId, serialCode);
        List<HQReturnUpdateRequest> requests = List.of(
                HQReturnUpdateRequest.builder()
                        .serialCode(serialCode)
                        .isInspected(true)
                        .status(ReturnItemStatus.NORMAL)
                        .build()
        );

        // when
        Map<Long, ReturnItemInspection> response = franchiseReturnService.updateReturnItemStatus(serialCodeByReturnItemId, requests);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturnItemIdIn(List.of(returnItemId));
        assertEquals(ReturnItemStatus.NORMAL, response.values().stream().findFirst().get().status());
        assertEquals(true, response.values().stream().findFirst().get().isInspected());
    }

    @Test
    @DisplayName("반품에 대한 반품 제품이 존재하지 않을 시 예외 발생")
    void updateReturnItemStatus_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findAllByReturnItemIdIn(List.of(returnItemId))).willReturn(List.of());

        Map<Long, String> serialCodeByReturnItemId = Map.of(returnItemId, serialCode);
        List<HQReturnUpdateRequest> requests = List.of(
                HQReturnUpdateRequest.builder()
                        .serialCode(serialCode)
                        .isInspected(true)
                        .status(ReturnItemStatus.NORMAL)
                        .build()
        );

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.updateReturnItemStatus(serialCodeByReturnItemId, requests);
        });
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 요청 상태 변경 - 성공")
    void updateReturnsStatus_Success() {
        // given
        given(franchiseReturnRepository.findAllByReturnCodeIn(List.of(returnCode))).willReturn(List.of(returns));

        // when
        List<ReturnInfo> response = franchiseReturnService.updateReturnStatus(List.of(returnCode));

        // then
        verify(franchiseReturnRepository, times(1)).findAllByReturnCodeIn(List.of(returnCode));
        assertEquals(ReturnStatus.ACCEPTED, response.stream().findFirst().get().status());
    }

    @Test
    @DisplayName("잘못된 값으로 반품 조회 시 예외 발생")
    void updateReturnsStatus_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findAllByReturnCodeIn(List.of(returnCode))).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () -> {
            franchiseReturnService.updateReturnStatus(List.of(returnCode));
        });
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }
}