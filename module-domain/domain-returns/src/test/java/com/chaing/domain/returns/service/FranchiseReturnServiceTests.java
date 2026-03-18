package com.chaing.domain.returns.service;

import com.chaing.core.enums.ReturnItemStatus;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.command.HQReturnDetailCommand;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCommand;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.HQReturnItemUpdateRequest;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
    String returnCode;
    String boxCode;
    String description;

    Returns returns;
    Returns acceptedReturns;
    ReturnItem returnItem;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;
        userId = 3L;
        orderId = 10L;
        returnId = 1L;
        returnItemId = 100L;
        orderItemId = 50L;
        returnCode = "RT-20260101-001";
        boxCode = "BOX-001";
        description = "제품 불량";

        returns = Returns.builder()
                .franchiseId(franchiseId)
                .franchiseOrderId(orderId)
                .returnCode(returnCode)
                .userId(userId)
                .description(description)
                .totalReturnQuantity(5)
                .totalReturnAmount(BigDecimal.valueOf(35000))
                .build();
        ReflectionTestUtils.setField(returns, "returnId", returnId);

        acceptedReturns = Returns.builder()
                .franchiseId(franchiseId)
                .franchiseOrderId(orderId)
                .returnCode(returnCode)
                .userId(userId)
                .description(description)
                .totalReturnQuantity(5)
                .totalReturnAmount(BigDecimal.valueOf(35000))
                .returnStatus(ReturnStatus.ACCEPTED)
                .build();
        ReflectionTestUtils.setField(acceptedReturns, "returnId", returnId);

        returnItem = ReturnItem.builder()
                .returns(returns)
                .franchiseOrderItemId(orderItemId)
                .boxCode(boxCode)
                .build();
        ReflectionTestUtils.setField(returnItem, "returnItemId", returnItemId);
    }

    // ==================== getAllReturns ====================

    @Test
    @DisplayName("반품 전체 조회 - 성공")
    void getAllReturns_GivenValidFranchiseId_ShouldReturnMap() {
        // given
        given(franchiseReturnRepository.findAllByFranchiseIdAndDeletedAtIsNull(franchiseId)).willReturn(List.of(returns));

        // when
        Map<Long, ReturnCommand> result = franchiseReturnService.getAllReturns(franchiseId);

        // then
        verify(franchiseReturnRepository, times(1)).findAllByFranchiseIdAndDeletedAtIsNull(franchiseId);
        assertEquals(1, result.size());
        assertEquals(returnCode, result.get(returnId).returnCode());
    }

    @Test
    @DisplayName("반품 전체 조회 - 빈 결과")
    void getAllReturns_GivenNoReturns_ShouldReturnEmptyMap() {
        // given
        given(franchiseReturnRepository.findAllByFranchiseIdAndDeletedAtIsNull(franchiseId)).willReturn(List.of());

        // when
        Map<Long, ReturnCommand> result = franchiseReturnService.getAllReturns(franchiseId);

        // then
        assertTrue(result.isEmpty());
    }

    // ==================== getReturn ====================

    @Test
    @DisplayName("반품 세부정보 조회 - 성공")
    void getReturn_GivenValidParams_ShouldReturnCommand() {
        // given
        given(franchiseReturnRepository.findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode))
                .willReturn(Optional.of(returns));

        // when
        ReturnCommand result = franchiseReturnService.getReturn(userId, franchiseId, returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode);
        assertEquals(returnCode, result.returnCode());
        assertEquals(ReturnStatus.PENDING, result.status());
    }

    @Test
    @DisplayName("반품 세부정보 조회 - RETURN_NOT_FOUND")
    void getReturn_GivenInvalidParams_ShouldThrowRETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByUserIdAndFranchiseIdAndReturnCodeAndDeletedAtIsNull(userId, franchiseId, returnCode))
                .willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getReturn(userId, franchiseId, returnCode));
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    // ==================== createReturn ====================

    @Test
    @DisplayName("반품 생성 - 성공")
    void createReturn_GivenValidRequest_ShouldReturnCommand() {
        // given
        FranchiseReturnCreateRequest request = new FranchiseReturnCreateRequest(
                "ORD-001", "SE", ReturnType.PRODUCT_DEFECT, description, 5, BigDecimal.valueOf(35000), List.of(boxCode));

        // when
        ReturnCommand result = franchiseReturnService.createReturn(franchiseId, orderId, returnCode, userId, request);

        // then
        verify(franchiseReturnRepository, times(1)).save(any(Returns.class));
        assertEquals(returnCode, result.returnCode());
        assertEquals(ReturnStatus.PENDING, result.status());
        assertEquals(5, result.quantity());
    }

    // ==================== createReturnItems ====================

    @Test
    @DisplayName("반품 제품 생성 - 성공")
    void createReturnItems_GivenValidRequest_ShouldReturnItems() {
        // given
        Map<Long, String> boxCodeByOrderItemId = Map.of(orderItemId, boxCode);
        given(franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)).willReturn(Optional.of(returns));

        // when
        List<ReturnItemCommand> result = franchiseReturnService.createReturnItems(returnId, boxCodeByOrderItemId);

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnIdAndDeletedAtIsNull(returnId);
        verify(franchiseReturnItemRepository, times(1)).saveAll(any());
        assertEquals(1, result.size());
        assertEquals(boxCode, result.get(0).boxCode());
    }

    // ==================== cancel ====================

    @Test
    @DisplayName("반품 취소 - 성공")
    void cancel_GivenPendingReturn_ShouldReturnCode() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode))
                .willReturn(Optional.of(returns));

        // when
        String result = franchiseReturnService.cancel(franchiseId, userId, returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode);
        assertEquals(returnCode, result);
    }

    @Test
    @DisplayName("반품 취소 - RETURN_NOT_FOUND")
    void cancel_GivenInvalidReturn_ShouldThrowRETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode))
                .willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.cancel(franchiseId, userId, returnCode));
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 취소 - CANCEL_NOT_ALLOWED (대기 상태가 아닌 경우)")
    void cancel_GivenAcceptedReturn_ShouldThrowCANCEL_NOT_ALLOWED() {
        // given
        given(franchiseReturnRepository.findByFranchiseIdAndUserIdAndReturnCode(franchiseId, userId, returnCode))
                .willReturn(Optional.of(acceptedReturns));

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.cancel(franchiseId, userId, returnCode));
        assertEquals(FranchiseReturnErrorCode.CANCEL_NOT_ALLOWED, exception.getErrorCode());
    }

    // ==================== acceptReturn ====================

    @Test
    @DisplayName("반품 접수 - 성공")
    void acceptReturn_GivenPendingReturns_ShouldReturnAccepted() {
        // given
        List<String> returnCodes = List.of(returnCode);
        given(franchiseReturnRepository.findAllByReturnCodeInAndDeletedAtIsNull(returnCodes)).willReturn(List.of(returns));

        // when
        List<ReturnCommand> result = franchiseReturnService.acceptReturn(returnCodes);

        // then
        verify(franchiseReturnRepository, times(1)).findAllByReturnCodeInAndDeletedAtIsNull(returnCodes);
        assertEquals(1, result.size());
        assertEquals(ReturnStatus.ACCEPTED, result.get(0).status());
    }

    @Test
    @DisplayName("반품 접수 - RETURN_NOT_FOUND")
    void acceptReturn_GivenInvalidReturnCodes_ShouldThrowRETURN_NOT_FOUND() {
        // given
        List<String> returnCodes = List.of(returnCode);
        given(franchiseReturnRepository.findAllByReturnCodeInAndDeletedAtIsNull(returnCodes)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.acceptReturn(returnCodes));
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 접수 - ALREADY_ACCEPTED (이미 접수된 상태)")
    void acceptReturn_GivenAlreadyAccepted_ShouldThrowALREADY_ACCEPTED() {
        // given
        List<String> returnCodes = List.of(returnCode);
        given(franchiseReturnRepository.findAllByReturnCodeInAndDeletedAtIsNull(returnCodes)).willReturn(List.of(acceptedReturns));

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.acceptReturn(returnCodes));
        assertEquals(FranchiseReturnErrorCode.ALREADY_ACCEPTED, exception.getErrorCode());
    }

    // ==================== getAllReturnByStatus ====================

    @Test
    @DisplayName("상태별 반품 조회 - 성공")
    void getAllReturnByStatus_GivenStatus_ShouldReturnMap() {
        // given
        given(franchiseReturnRepository.findAllByReturnStatus(ReturnStatus.PENDING)).willReturn(List.of(returns));

        // when
        Map<Long, HQReturnCommand> result = franchiseReturnService.getAllReturnByStatus(ReturnStatus.PENDING);

        // then
        verify(franchiseReturnRepository, times(1)).findAllByReturnStatus(ReturnStatus.PENDING);
        assertEquals(1, result.size());
        assertEquals(returnCode, result.get(returnId).returnCode());
    }

    // ==================== getAllReturn ====================

    @Test
    @DisplayName("전체 반품 조회 - 성공")
    void getAllReturn_ShouldReturnMap() {
        // given
        given(franchiseReturnRepository.findAllByDeletedAtIsNull()).willReturn(List.of(returns));

        // when
        Map<Long, HQReturnCommand> result = franchiseReturnService.getAllReturn();

        // then
        verify(franchiseReturnRepository, times(1)).findAllByDeletedAtIsNull();
        assertEquals(1, result.size());
        assertEquals(returnCode, result.get(returnId).returnCode());
    }

    // ==================== getHQReturnInfo ====================

    @Test
    @DisplayName("본사 반품 상세 조회 - 성공")
    void getHQReturnInfo_GivenValidReturnCode_ShouldReturnDetail() {
        // given
        given(franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(Optional.of(returns));

        // when
        HQReturnDetailCommand result = franchiseReturnService.getHQReturnInfo(returnCode);

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(returnCode, result.returnCode());
        assertEquals(franchiseId, result.franchiseId());
    }

    @Test
    @DisplayName("본사 반품 상세 조회 - RETURN_NOT_FOUND")
    void getHQReturnInfo_GivenInvalidReturnCode_ShouldThrowRETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getHQReturnInfo(returnCode));
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    // ==================== getReturnItemId ====================

    @Test
    @DisplayName("반품 제품 ID 맵 조회 - 성공")
    void getReturnItemId_GivenValidReturnCode_ShouldReturnMap() {
        // given
        given(franchiseReturnItemRepository.findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode)).willReturn(List.of(returnItem));

        // when
        Map<Long, Long> result = franchiseReturnService.getReturnItemId(returnCode);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnCodeAndDeletedAtIsNull(returnCode);
        assertEquals(orderItemId, result.get(returnItemId));
    }

    // ==================== getReturnItemsByReturnId ====================

    @Test
    @DisplayName("반품 ID로 제품 조회 - 성공")
    void getReturnItemsByReturnId_GivenValidReturnId_ShouldReturnMap() {
        // given
        given(franchiseReturnItemRepository.findByReturns_ReturnIdAndDeletedAtIsNull(returnId)).willReturn(List.of(returnItem));

        // when
        Map<Long, ReturnItemCommand> result = franchiseReturnService.getReturnItemsByReturnId(returnId);

        // then
        verify(franchiseReturnItemRepository, times(1)).findByReturns_ReturnIdAndDeletedAtIsNull(returnId);
        assertEquals(1, result.size());
        assertEquals(boxCode, result.get(returnItemId).boxCode());
    }

    @Test
    @DisplayName("반품 ID로 제품 조회 - RETURN_ITEM_NOT_FOUND")
    void getReturnItemsByReturnId_GivenNoItems_ShouldThrowRETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findByReturns_ReturnIdAndDeletedAtIsNull(returnId)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getReturnItemsByReturnId(returnId));
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    // ==================== getAllReturnItemByReturnIds ====================

    @Test
    @DisplayName("반품 ID 목록으로 제품 조회 - 성공")
    void getAllReturnItemByReturnIds_GivenValidIds_ShouldReturnGroupedMap() {
        // given
        List<Long> returnIds = List.of(returnId);
        given(franchiseReturnItemRepository.findAllByReturns_ReturnIdInAndDeletedAtIsNull(returnIds)).willReturn(List.of(returnItem));

        // when
        Map<Long, List<ReturnItemCommand>> result = franchiseReturnService.getAllReturnItemByReturnIds(returnIds);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnIdInAndDeletedAtIsNull(returnIds);
        assertEquals(1, result.get(returnId).size());
        assertEquals(boxCode, result.get(returnId).get(0).boxCode());
    }

    // ==================== inspectReturnItems ====================

    @Test
    @DisplayName("반품 제품 검수 - 성공")
    void inspectReturnItems_GivenValidRequest_ShouldReturnStatusMap() {
        // given
        HQReturnItemUpdateRequest itemRequest = HQReturnItemUpdateRequest.builder()
                .boxCode(boxCode)
                .serialCode("SN-001")
                .isInspected(true)
                .status(ReturnItemStatus.NORMAL)
                .build();
        HQReturnUpdateRequest request = new HQReturnUpdateRequest(ReturnStatus.PENDING, List.of(itemRequest));

        given(franchiseReturnItemRepository.findAllByReturns_ReturnIdAndDeletedAtIsNull(returnId)).willReturn(List.of(returnItem));

        // when
        Map<String, ReturnItemStatus> result = franchiseReturnService.inspectReturnItems(returnId, request);

        // then
        verify(franchiseReturnItemRepository, times(1)).findAllByReturns_ReturnIdAndDeletedAtIsNull(returnId);
        verify(franchiseReturnItemRepository, times(1)).saveAll(any());
        assertEquals(ReturnItemStatus.NORMAL, result.get(boxCode));
    }

    @Test
    @DisplayName("반품 제품 검수 - RETURN_ITEM_NOT_FOUND (제품 없음)")
    void inspectReturnItems_GivenNoItems_ShouldThrowRETURN_ITEM_NOT_FOUND() {
        // given
        HQReturnItemUpdateRequest itemRequest = HQReturnItemUpdateRequest.builder()
                .boxCode(boxCode)
                .serialCode("SN-001")
                .isInspected(true)
                .status(ReturnItemStatus.NORMAL)
                .build();
        HQReturnUpdateRequest request = new HQReturnUpdateRequest(ReturnStatus.PENDING, List.of(itemRequest));

        given(franchiseReturnItemRepository.findAllByReturns_ReturnIdAndDeletedAtIsNull(returnId)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.inspectReturnItems(returnId, request));
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    // ==================== updateReturnStatusInInspection ====================

    @Test
    @DisplayName("검수 중 반품 상태 수정 - 성공")
    void updateReturnStatusInInspection_GivenValidStatus_ShouldReturnStatus() {
        // given
        given(franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)).willReturn(Optional.of(returns));

        // when
        ReturnStatus result = franchiseReturnService.updateReturnStatusInInspection(returnId, ReturnStatus.PENDING);

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnIdAndDeletedAtIsNull(returnId);
        assertEquals(ReturnStatus.PENDING, result);
    }

    @Test
    @DisplayName("검수 중 반품 상태 수정 - RETURN_NOT_FOUND")
    void updateReturnStatusInInspection_GivenInvalidReturnId_ShouldThrowRETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.updateReturnStatusInInspection(returnId, ReturnStatus.PENDING));
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    // ==================== updateShippingPending ====================

    @Test
    @DisplayName("배송대기 상태 수정 - 성공")
    void updateShippingPending_GivenAcceptedReturns_ShouldReturnMap() {
        // given
        Set<String> returnCodes = Set.of(returnCode);
        given(franchiseReturnRepository.findAllByReturnCodeInAndDeletedAtIsNull(returnCodes)).willReturn(List.of(acceptedReturns));

        // when
        Map<Long, ReturnCommand> result = franchiseReturnService.updateShippingPending(returnCodes);

        // then
        verify(franchiseReturnRepository, times(1)).findAllByReturnCodeInAndDeletedAtIsNull(returnCodes);
        assertEquals(1, result.size());
        assertEquals(ReturnStatus.SHIPPING_PENDING, result.get(returnId).status());
    }

    // ==================== getReturnByReturnId ====================

    @Test
    @DisplayName("반품 엔티티 조회 - 성공")
    void getReturnByReturnId_GivenValidId_ShouldReturnEntity() {
        // given
        given(franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)).willReturn(Optional.of(returns));

        // when
        Returns result = franchiseReturnService.getReturnByReturnId(returnId);

        // then
        verify(franchiseReturnRepository, times(1)).findByReturnIdAndDeletedAtIsNull(returnId);
        assertEquals(returnCode, result.getReturnCode());
    }

    @Test
    @DisplayName("반품 엔티티 조회 - RETURN_NOT_FOUND")
    void getReturnByReturnId_GivenInvalidId_ShouldThrowRETURN_NOT_FOUND() {
        // given
        given(franchiseReturnRepository.findByReturnIdAndDeletedAtIsNull(returnId)).willReturn(Optional.empty());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getReturnByReturnId(returnId));
        assertEquals(FranchiseReturnErrorCode.RETURN_NOT_FOUND, exception.getErrorCode());
    }

    // ==================== getReturnItemListByReturnId ====================

    @Test
    @DisplayName("반품 아이템 리스트 조회 - 성공")
    void getReturnItemListByReturnId_GivenValidId_ShouldReturnList() {
        // given
        given(franchiseReturnItemRepository.findByReturns_ReturnIdAndDeletedAtIsNull(returnId)).willReturn(List.of(returnItem));

        // when
        List<ReturnItem> result = franchiseReturnService.getReturnItemListByReturnId(returnId);

        // then
        verify(franchiseReturnItemRepository, times(1)).findByReturns_ReturnIdAndDeletedAtIsNull(returnId);
        assertEquals(1, result.size());
        assertEquals(boxCode, result.get(0).getBoxCode());
    }

    @Test
    @DisplayName("반품 아이템 리스트 조회 - RETURN_ITEM_NOT_FOUND")
    void getReturnItemListByReturnId_GivenNoItems_ShouldThrowRETURN_ITEM_NOT_FOUND() {
        // given
        given(franchiseReturnItemRepository.findByReturns_ReturnIdAndDeletedAtIsNull(returnId)).willReturn(List.of());

        // when & then
        FranchiseReturnException exception = assertThrows(FranchiseReturnException.class, () ->
                franchiseReturnService.getReturnItemListByReturnId(returnId));
        assertEquals(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND, exception.getErrorCode());
    }
}
