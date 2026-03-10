/*
package com.chaing.domain.returns.service;

import com.chaing.core.dto.command.FranchiseInventoryCommand;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.command.HQReturnDetailCommand;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCommand;
import com.chaing.domain.returns.dto.command.ReturnItemInspection;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.HQReturnItemUpdateRequest;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnItemStatus;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.repository.FranchiseReturnItemRepository;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
    private ReturnCodeGenerator generator;

    Long franchiseId;
    Long userId;
    Long orderId;
    Long returnId;
    Long returnItemId;
    Long orderItemId;
    Long inventoryId;

    String returnCode;
    String description;
    String boxCode;
    String serialCode;

    Integer quantity;
    BigDecimal totalAmount;

    Returns returns;
    Returns acceptedReturns;
    ReturnItem returnItem;

    FranchiseReturnCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;
        userId = 1L;
        orderId = 10L;
        returnId = 2L;
        returnItemId = 3L;
        orderItemId = 100L;
        inventoryId = 200L;

        returnCode = "RET-0001";
        description = "description";
        boxCode = "BOX-001";
        serialCode = "SERIAL-001";

        quantity = 3;
        totalAmount = new BigDecimal("30000");

        returns = Returns.builder()
                .franchiseId(franchiseId)
                .franchiseOrderId(orderId)
                .returnCode(returnCode)
                .userId(userId)
                .description(description)
                .totalReturnQuantity(quantity)
                .totalReturnAmount(totalAmount)
                .build();
        ReflectionTestUtils.setField(returns, "returnId", returnId);

        returnItem = ReturnItem.builder()
                .returns(returns)
                .franchiseOrderItemId(orderItemId)
                .boxCode(boxCode)
                .build();
        ReflectionTestUtils.setField(returnItem, "returnItemId", returnItemId);

        acceptedReturns = Returns.builder()
                .franchiseId(franchiseId)
                .franchiseOrderId(orderId)
                .returnCode(returnCode)
                .userId(userId)
                .description(description)
                .totalReturnQuantity(quantity)
                .totalReturnAmount(totalAmount)
                .returnStatus(ReturnStatus.ACCEPTED)
                .build();

        createRequest = new FranchiseReturnCreateRequest(
                "ORD-001",
                null,
                ReturnType.PRODUCT_DEFECT,
                description,
                quantity,
                totalAmount,
                List.of(boxCode)
        );
    }

    @Test
    @DisplayName("가맹점 반품 전체 조회 - 성공")
    void getAllReturns_Success() {
        // given
        given(franchiseReturnRepository.findAllByFranchiseIdAndDeletedAtIsNull(franchiseId)).willReturn(List.of(returns));

        // when
        Map<Long, ReturnCommand> result = franchiseReturnService.getAllReturns(franchiseId);

        // then
        verify(franchiseReturnRepository, times(1)).findAllByFranchiseIdAndDeletedAtIsNull(franchiseId);
        assertEquals(returnId, result.get(returnId).returnId());
        assertEquals(returnCode, result.get(returnId).returnCode());
    }

    @Test
    @DisplayName("반품 데이터 없을 때 전체 조회 시 예외 발생")
    void getAllReturns_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findAllByFranchiseIdAndDeletedAtIsNull(franchiseId)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getAllReturns(franchiseId));
        verify(franchiseReturnRepository, times(1)).findAllByFranchiseIdAndDeletedAtIsNull(franchiseId);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 코드로 특정 반품 조회 - 성공")
    void getReturn_Success() {
        // given
        given(franchiseReturnRepository.findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode))
                .willReturn(Optional.of(returns));

        // when
        ReturnCommand result = franchiseReturnService.getReturn(userId, franchiseId, returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode);
        assertEquals(returnCode, result.returnCode());
        assertEquals(returnId, result.returnId());
    }

    @Test
    @DisplayName("존재하지 않는 반품 코드로 조회 시 예외 발생")
    void getReturn_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode))
                .willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getReturn(userId, franchiseId, returnCode));
        verify(franchiseReturnRepository, times(1)).findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 취소 - 성공")
    void cancel_Success() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode))
                .willReturn(Optional.of(returns));

        // when
        String result = franchiseReturnService.cancel(franchiseId, userId, returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode);
        assertEquals(returnCode, result);
        assertEquals(ReturnStatus.CANCELED, returns.getReturnStatus());
    }

    @Test
    @DisplayName("존재하지 않는 반품 코드로 취소 시 예외 발생")
    void cancel_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode))
                .willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.cancel(franchiseId, userId, returnCode));
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대기 상태가 아닌 반품 취소 시 예외 발생")
    void cancel_Failure_CANCEL_NOT_ALLOWED() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode))
                .willReturn(Optional.of(acceptedReturns));

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.cancel(franchiseId, userId, returnCode));
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode);
        assertEquals(FranchiseReturnErrorCode.CANCEL_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 생성 - 성공")
    void createReturn_Success() {
        // when
        ReturnCommand result = franchiseReturnService.createReturn(franchiseId, orderId, returnCode, userId, createRequest);

        // then
        assertEquals(returnCode, result.returnCode());
        assertEquals(ReturnStatus.PENDING, result.status());
        assertEquals(orderId, result.orderId());
    }

    @Test
    @DisplayName("반품 제품 생성 - 성공")
    void createReturnItems_Success() {
        // given
        Map<Long, String> boxCodeByOrderItemId = Map.of(orderItemId, boxCode);
        given(franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)).willReturn(Optional.of(returns));

        // when
        List<ReturnItemCommand> result = franchiseReturnService.createReturnItems(returnId, boxCodeByOrderItemId);

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnIdAndDeletedAtIsNull(returnId);
        assertEquals(1, result.size());
        assertEquals(orderItemId, result.get(0).orderItemId());
        assertEquals(boxCode, result.get(0).boxCode());
    }

    @Test
    @DisplayName("존재하지 않는 반품 ID로 반품 제품 생성 시 예외 발생")
    void createReturnItems_Failure_RETURN_NOT_FOUND() {
        // given
        Map<Long, String> boxCodeByOrderItemId = Map.of(orderItemId, boxCode);
        given(franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.createReturnItems(returnId, boxCodeByOrderItemId));
        verify(franchiseReturnRepository, times(1)).findByReturnIdAndDeletedAtIsNull(returnId);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("상태별 반품 목록 조회 - 성공")
    void getAllReturnByStatus_Success() {
        // given
        given(franchiseReturnRepository.findAllByReturnStatus(ReturnStatus.PENDING)).willReturn(List.of(returns));

        // when
        Map<Long, HQReturnCommand> result = franchiseReturnService.getAllReturnByStatus(ReturnStatus.PENDING);

        // then
        verify(franchiseReturnRepository, times(1)).findAllByReturnStatus(ReturnStatus.PENDING);
        assertEquals(ReturnStatus.PENDING, result.values().stream().findFirst().get().status());
    }

    @Test
    @DisplayName("대기 상태가 아닌 반품 목록 조회 - 성공")
    void getAllReturn_Success() {
        // given
        given(franchiseReturnRepository.findAllByDeletedAtIsNull()).willReturn(List.of(acceptedReturns));

        // when
        Map<Long, HQReturnCommand> result = franchiseReturnService.getAllReturn();

        // then
        verify(franchiseReturnRepository, times(1)).findAllByDeletedAtIsNull();
        assertEquals(ReturnStatus.ACCEPTED, result.values().stream().findFirst().get().status());
    }

    @Test
    @DisplayName("본사 특정 반품 상세 조회 - 성공")
    void getHQReturnInfo_Success() {
        // given
        given(franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(Optional.of(returns));

        // when
        HQReturnDetailCommand result = franchiseReturnService.getHQReturnInfo(returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(returnId, result.returnId());
        assertEquals(returnCode, result.returnCode());
        assertEquals(orderId, result.franchiseOrderId());
    }

    @Test
    @DisplayName("존재하지 않는 반품 코드로 본사 상세 조회 시 예외 발생")
    void getHQReturnInfo_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getHQReturnInfo(returnCode));
        verify(franchiseReturnRepository, times(1)).findByReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 코드로 반품 제품 ID 맵 조회 - 성공")
    void getReturnItemId_Success() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(List.of(returnItem));

        // when
        Map<Long, Long> result = franchiseReturnService.getReturnItemId(returnCode);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(orderItemId, result.get(returnItemId));
    }

    @Test
    @DisplayName("반품 제품 없을 때 ID 맵 조회 시 예외 발생")
    void getReturnItemId_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getReturnItemId(returnCode));
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 제품 검수 상태 조회 - 성공")
    void getReturnItemInspection_Success() {
        // given
        given(franchiseReturnItemRepository.findAllByReturnItemIdIn(List.of(returnItemId))).willReturn(List.of(returnItem));

        // when
        Map<Long, ReturnItemInspection> result = franchiseReturnService.getReturnItemInspection(List.of(returnItemId));

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturnItemIdIn(List.of(returnItemId));
        assertEquals(false, result.get(returnItemId).isInspected());
        assertEquals(ReturnItemStatus.BEFORE_INSPECTION, result.get(returnItemId).status());
    }

    @Test
    @DisplayName("존재하지 않는 반품 제품 ID로 검수 상태 조회 시 예외 발생")
    void getReturnItemInspection_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findAllByReturnItemIdIn(List.of(returnItemId))).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getReturnItemInspection(List.of(returnItemId)));
        verify(franchiseReturnItemRepository, times(1)).findAllByReturnItemIdIn(List.of(returnItemId));
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 제품 검수 상태 업데이트 - 성공")
    void updateReturnItemStatus_Success() {
        // given
        given(franchiseReturnItemRepository.findAllByReturnItemIdIn(List.of(returnItemId))).willReturn(List.of(returnItem));
        Map<Long, String> serialCodeByReturnItemId = Map.of(returnItemId, serialCode);
        List<HQReturnItemUpdateRequest> requests = List.of(
                HQReturnItemUpdateRequest.builder()
                        .boxCode(boxCode)
                        .serialCode(serialCode)
                        .isInspected(true)
                        .status(ReturnItemStatus.NORMAL)
                        .build()
        );

        // when
        Map<Long, ReturnItemInspection> result = franchiseReturnService.updateReturnItemStatus(serialCodeByReturnItemId, requests);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturnItemIdIn(List.of(returnItemId));
        assertEquals(true, result.get(returnItemId).isInspected());
        assertEquals(ReturnItemStatus.NORMAL, result.get(returnItemId).status());
    }

    @Test
    @DisplayName("존재하지 않는 반품 제품 ID로 검수 상태 업데이트 시 예외 발생")
    void updateReturnItemStatus_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findAllByReturnItemIdIn(List.of(returnItemId))).willReturn(List.of());
        Map<Long, String> serialCodeByReturnItemId = Map.of(returnItemId, serialCode);
        List<HQReturnItemUpdateRequest> requests = List.of(
                HQReturnItemUpdateRequest.builder()
                        .boxCode(boxCode)
                        .serialCode(serialCode)
                        .isInspected(true)
                        .status(ReturnItemStatus.NORMAL)
                        .build()
        );

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.updateReturnItemStatus(serialCodeByReturnItemId, requests));
        verify(franchiseReturnItemRepository, times(1)).findAllByReturnItemIdIn(List.of(returnItemId));
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 요청 상태 변경 - 성공")
    void updateReturnStatus_Success() {
        // given
        given(franchiseReturnRepository.findAllByReturnCodeIn(List.of(returnCode))).willReturn(List.of(returns));

        // when
        List<ReturnInfo> result = franchiseReturnService.updateReturnStatus(List.of(returnCode));

        // then
        verify(franchiseReturnRepository, times(1)).findAllByReturnCodeIn(List.of(returnCode));
        assertEquals(ReturnStatus.ACCEPTED, result.get(0).status());
    }

    @Test
    @DisplayName("존재하지 않는 반품 코드로 상태 변경 시 예외 발생")
    void updateReturnStatus_Failure_RETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findAllByReturnCodeIn(List.of(returnCode))).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.updateReturnStatus(List.of(returnCode)));
        verify(franchiseReturnRepository, times(1)).findAllByReturnCodeIn(List.of(returnCode));
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("여러 반품 ID로 반품 제품 목록 조회 - 성공")
    void getAllReturnItemByReturnIds_Success() {
        // given
        List<Long> returnIds = List.of(returnId);
        given(franchiseReturnItemRepository.findAllByReturns_ReturnIdInAndDeletedAtIsNull(returnIds)).willReturn(List.of(returnItem));

        // when
        Map<Long, List<ReturnItemCommand>> result = franchiseReturnService.getAllReturnItemByReturnIds(returnIds);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnIdInAndDeletedAtIsNull(returnIds);
        assertEquals(1, result.get(returnId).size());
        assertEquals(returnItemId, result.get(returnId).get(0).returnItemId());
    }

    @Test
    @DisplayName("반품 제품 없을 때 여러 반품 ID 조회 시 예외 발생")
    void getAllReturnItemByReturnIds_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        List<Long> returnIds = List.of(returnId);
        given(franchiseReturnItemRepository.findAllByReturns_ReturnIdInAndDeletedAtIsNull(returnIds)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getAllReturnItemByReturnIds(returnIds));
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnIdInAndDeletedAtIsNull(returnIds);
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("단일 반품 ID로 반품 제품 맵 조회 - 성공")
    void getReturnItemsByReturnId_Success() {
        // given
        given(franchiseReturnItemRepository.findByReturns_ReturnIdAndDeletedAtIsNull(returnId)).willReturn(List.of(returnItem));

        // when
        Map<Long, ReturnItemCommand> result = franchiseReturnService.getReturnItemsByReturnId(returnId);

        // then
        verify(franchiseReturnItemRepository, times(1)).findByReturns_ReturnIdAndDeletedAtIsNull(returnId);
        assertEquals(returnItemId, result.get(returnItemId).returnItemId());
        assertEquals(orderItemId, result.get(returnItemId).orderItemId());
    }

    @Test
    @DisplayName("반품 제품 없을 때 단일 반품 ID 조회 시 예외 발생")
    void getReturnItemsByReturnId_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findByReturns_ReturnIdAndDeletedAtIsNull(returnId)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getReturnItemsByReturnId(returnId));
        verify(franchiseReturnItemRepository, times(1)).findByReturns_ReturnIdAndDeletedAtIsNull(returnId);
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 제품 수정 - 성공")
    void updateReturnItems_Success() {
        // given
        FranchiseInventoryCommand inventory = FranchiseInventoryCommand.builder()
                .inventoryId(inventoryId)
                .orderItemId(orderItemId)
                .boxCode(boxCode)
                .build();
        Map<String, FranchiseInventoryCommand> inventoryByBoxCode = Map.of(boxCode, inventory);
        given(franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(Optional.of(returns));
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode))
                .willReturn(List.of(returnItem));

        // when
        List<ReturnItemCommand> result = franchiseReturnService.updateReturnItems(List.of(boxCode), returnCode, inventoryByBoxCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnCodeAndDeletedAtIsNull(returnCode);
        verify(franchiseReturnItemRepository, times(2)).findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(1, result.size());
        assertEquals(boxCode, result.get(0).boxCode());
    }

    @Test
    @DisplayName("존재하지 않는 반품 코드로 반품 제품 수정 시 예외 발생")
    void updateReturnItems_Failure_RETURN_NOT_FOUND() {
        // given
        FranchiseInventoryCommand inventory = FranchiseInventoryCommand.builder()
                .inventoryId(inventoryId)
                .orderItemId(orderItemId)
                .boxCode(boxCode)
                .build();
        Map<String, FranchiseInventoryCommand> inventoryByBoxCode = Map.of(boxCode, inventory);
        given(franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.updateReturnItems(List.of(boxCode), returnCode, inventoryByBoxCode));
        verify(franchiseReturnRepository, times(1)).findByReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 제품 없을 때 수정 시 예외 발생")
    void updateReturnItems_Failure_RETURN_ITEM_NOT_FOUND() {
        // given
        FranchiseInventoryCommand inventory = FranchiseInventoryCommand.builder()
                .inventoryId(inventoryId)
                .orderItemId(orderItemId)
                .boxCode(boxCode)
                .build();
        Map<String, FranchiseInventoryCommand> inventoryByBoxCode = Map.of(boxCode, inventory);
        given(franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(Optional.of(returns));
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.updateReturnItems(List.of(boxCode), returnCode, inventoryByBoxCode));
        verify(franchiseReturnRepository, times(1)).findByReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }
}
*/
