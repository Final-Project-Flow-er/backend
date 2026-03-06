package com.chaing.domain.orders.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequest;
import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequestItem;
import com.chaing.domain.orders.dto.request.FranchiseOrderUpdateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
import com.chaing.domain.orders.dto.response.FranchiseOrderCancelResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderItemDetailResponse;
import com.chaing.domain.orders.dto.response.HQOrderStatusUpdateResponse;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.FranchiseOrderErrorCode;
import com.chaing.domain.orders.exception.FranchiseOrderException;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
import com.chaing.domain.orders.repository.FranchiseOrderItemRepository;
import com.chaing.domain.orders.repository.FranchiseOrderRepository;
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

@ExtendWith(MockitoExtension.class)
class FranchiseOrderServiceTests {
    @InjectMocks
    private FranchiseOrderService franchiseOrderService;

    @Mock
    private FranchiseOrderRepository franchiseOrderRepository;

    @Mock
    private FranchiseOrderItemRepository franchiseOrderItemRepository;

    Long franchiseId;
    String username;
    Long userId;

    Long franchiseOrderId;
    String orderCode;
    String phoneNumber;
    String address;
    String requirement;
    FranchiseOrderStatus status;
    Integer totalQuantity;
    BigDecimal totalAmount;
    LocalDateTime deliveryDate;
    String deliveryTime;
    String serialCode;

    Long franchiseOrderItemId;
    Long productId;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;

    String productCode;
    String productName;

    FranchiseOrder franchiseOrder;
    FranchiseOrderItem franchiseOrderItem;

    FranchiseOrder shippingFranchiseOrder;

    HQOrderUpdateStatusRequest hqOrderUpdateStatusRequestAccept;
    HQOrderUpdateStatusRequest hqOrderUpdateStatusRequestReject;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;
        userId = 1L;
        username = "test";
        serialCode = "SerialCode";

        franchiseOrderId = 1L;
        orderCode = "test";
        phoneNumber = "test";
        address = "test";
        requirement = "test";
        status = FranchiseOrderStatus.PENDING;
        totalQuantity = 10;
        totalAmount = new BigDecimal(100000);
        deliveryDate = LocalDateTime.now();
        deliveryTime = "11:00";

        franchiseOrderItemId = 10L;
        productId = 10L;
        quantity = 10;
        unitPrice = new BigDecimal(10000);
        totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        productCode = "productCode";
        productName = "productName";

        franchiseOrder = FranchiseOrder.builder()
                .franchiseOrderId(franchiseOrderId)
                .franchiseId(franchiseId)
                .userId(userId)
                .orderCode(orderCode)
                .address(address)
                .requirement(requirement)
                .orderStatus(status)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .deliveryDate(deliveryDate)
                .deliveryTime(deliveryTime)
                .build();
        ReflectionTestUtils.setField(franchiseOrder, "franchiseOrderId", franchiseOrderId);

        franchiseOrderItem = FranchiseOrderItem.builder()
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
        ReflectionTestUtils.setField(franchiseOrderItem, "franchiseOrderItemId", franchiseOrderItemId);

        franchiseOrder.addOrderItem(franchiseOrderItem);

        hqOrderUpdateStatusRequestAccept = HQOrderUpdateStatusRequest.builder()
                .orderCodes(List.of(orderCode))
                .isAccepted(true)
                .build();

        hqOrderUpdateStatusRequestReject = HQOrderUpdateStatusRequest.builder()
                .orderCodes(List.of(orderCode))
                .isAccepted(false)
                .build();

        shippingFranchiseOrder = FranchiseOrder.builder()
                .orderStatus(FranchiseOrderStatus.SHIPPING)
                .build();
    }

    @Test
    @DisplayName("가맹점 발주 목록 조회 - 성공")
    void getAllOrders_Success() {
        // given
        given(franchiseOrderRepository.findAllByFranchiseIdAndUserId(franchiseId, userId)).willReturn(List.of(franchiseOrder));

        // when
        Map<Long, FranchiseOrderCommand> orders = franchiseOrderService.getAllOrders(franchiseId, userId);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByFranchiseIdAndUserId(franchiseId, userId);
        assertEquals(franchiseOrderId, orders.get(franchiseOrderId).orderId());
    }

    @Test
    @DisplayName("잘못된 값으로 발주 조회 시 예외 발생")
    void getAlOrders_Failure_ORDER_NOT_FOUND() {
        // given
        given(franchiseOrderRepository.findAllByFranchiseIdAndUserId(franchiseId, userId)).willReturn(List.of());

        // when & then
        OrderException exception = assertThrows(OrderException.class, () -> {
            franchiseOrderService.getAllOrders(franchiseId, userId);
        });
        verify(franchiseOrderRepository, times(1)).findAllByFranchiseIdAndUserId(franchiseId, userId);
        assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 번호로 가맹점 특정 발주 조회 - 성공")
    void getOrder_Success() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode)).willReturn(Optional.of(franchiseOrder));

        // when
        FranchiseOrderDetailCommand response = franchiseOrderService.getOrderByOrderCode(franchiseId, userId, orderCode);

        // then
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode);
        assertEquals(franchiseOrderId, response.orderId());
    }

    @Test
    @DisplayName("존재하지 않는 발주 코드로 조회 시 예외 발생")
    void getOrder_Failure_ORDER_NOT_FOUND() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode)).willReturn(Optional.empty());

        // when & then
        OrderException exception = assertThrows(OrderException.class, () -> {
            franchiseOrderService.getOrderByOrderCode(franchiseId, userId, orderCode);
        });
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode);
        assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("가맹점 발주 수정 - 성공")
    void updateOrder_Success() {
        // given
        Map<Long, FranchiseOrderUpdateRequest> requestByProductId = new HashMap<>();
        Map<Long, ProductInfo> productInfoByProductId = new HashMap<>();
        requestByProductId.put(productId, FranchiseOrderUpdateRequest.builder()
                .productCode(productCode)
                .quantity(quantity)
                .build());
        productInfoByProductId.put(productId, ProductInfo.builder()
                        .productId(productId)
                        .productCode(productCode)
                        .productName(productName)
                        .retailPrice(unitPrice)
                        .build());

        given(franchiseOrderRepository.findByFranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId)).willReturn(Optional.of(franchiseOrder));
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId)).willReturn(List.of(franchiseOrderItem));

        // when
        List<FranchiseOrderItemDetailResponse> responses = franchiseOrderService.updateOrder(franchiseOrderId, requestByProductId, productInfoByProductId);

        // then
        verify(franchiseOrderRepository, times(1)).findByFranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        assertEquals(productCode, responses.get(0).productCode());
        assertEquals(productName, responses.get(0).productName());
        assertEquals(10 , responses.get(0).quantity());
        assertEquals(unitPrice, responses.get(0).unitPrice());
        assertEquals(totalPrice, responses.get(0).totalPrice());
    }

    @Test
    @DisplayName("발주 상태가 PENDING이 아닐 때 수정 시도 시 예외 발생")
    void updateOrder_Failure_ORDER_INVALID_STATUS() {
        // given
        Map<Long, FranchiseOrderUpdateRequest> requestByProductId = new HashMap<>();
        Map<Long, ProductInfo> productInfoByProductId = new HashMap<>();
        requestByProductId.put(productId, FranchiseOrderUpdateRequest.builder()
                .productCode(productCode)
                .quantity(quantity)
                .build());
        productInfoByProductId.put(productId, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .productName(productName)
                .retailPrice(unitPrice)
                .build());

        given(franchiseOrderRepository.findByFranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId)).willReturn(Optional.of(shippingFranchiseOrder));

        // when & then
        OrderException exception = assertThrows(OrderException.class, () -> {
            franchiseOrderService.updateOrder(franchiseOrderId, requestByProductId, productInfoByProductId);
        });
        verify(franchiseOrderRepository, times(1)).findByFranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        assertEquals(OrderErrorCode.INVALID_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 제품 주문 시 예외 발생")
    void updateOrder_Failure_ORDER_NOT_FOUND () {
        // given
        Map<Long, FranchiseOrderUpdateRequest> requestByProductId = new HashMap<>();
        Map<Long, ProductInfo> productInfoByProductId = new HashMap<>();
        requestByProductId.put(productId, FranchiseOrderUpdateRequest.builder()
                .productCode(productCode)
                .quantity(quantity)
                .build());
        productInfoByProductId.put(productId, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .productName(productName)
                .retailPrice(unitPrice)
                .build());

        given(franchiseOrderRepository.findByFranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId)).willReturn(Optional.of(franchiseOrder));
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId)).willReturn(List.of());

        // when & then
        OrderException exception = assertThrows(OrderException.class, () -> {
            franchiseOrderService.updateOrder(franchiseOrderId, requestByProductId, productInfoByProductId);
        });
        verify(franchiseOrderRepository, times(1)).findByFranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        assertEquals(OrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("1개 미만의 수량으로 수정 시 예외 발생")
    void updateOrder_Failure_INVALID_QUANTITY() {
        // given
        Map<Long, FranchiseOrderUpdateRequest> requestByProductId = new HashMap<>();
        Map<Long, ProductInfo> productInfoByProductId = new HashMap<>();
        requestByProductId.put(productId, FranchiseOrderUpdateRequest.builder()
                .productCode(productCode)
                .quantity(0)
                .build());
        productInfoByProductId.put(productId, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .productName(productName)
                .retailPrice(unitPrice)
                .build());

        given(franchiseOrderRepository.findByFranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId)).willReturn(Optional.of(franchiseOrder));
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId)).willReturn(List.of(franchiseOrderItem));

        // when & then
        OrderException exception = assertThrows(OrderException.class, () -> {
            franchiseOrderService.updateOrder(franchiseOrderId, requestByProductId, productInfoByProductId);
        });
        verify(franchiseOrderRepository, times(1)).findByFranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        assertEquals(OrderErrorCode.INVALID_QUANTITY, exception.getErrorCode());
    }

    @Test
    @DisplayName("가맹점 발주 취소 - 성공")
    void cancelOrder_Success() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode)).willReturn(Optional.of(franchiseOrder));

        // when
        FranchiseOrderCancelResponse response = franchiseOrderService.cancelOrder(userId, franchiseId, orderCode);

        // then
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode);
        assertEquals(FranchiseOrderStatus.CANCELED, response.status());
    }

    @Test
    @DisplayName("잘못된 값으로 발주 조회 시 예외 발생")
    void cancelOrder_Failure_ORDER_NOT_FOUND () {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode)).willReturn(Optional.empty());

        // when & then
        OrderException exception = assertThrows(OrderException.class, () -> {
            franchiseOrderService.cancelOrder(userId, franchiseId, orderCode);
        });
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode);
        assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잘못된 값으로 발주 조회 시 예외 발생")
    void cancelOrder_Failure_INVALID_STATUS () {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode)).willReturn(Optional.of(shippingFranchiseOrder));

        // when & then
        OrderException exception = assertThrows(OrderException.class, () -> {
            franchiseOrderService.cancelOrder(userId, franchiseId, orderCode);
        });
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode);
        assertEquals(OrderErrorCode.INVALID_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("가맹점 발주 생성 - 성공")
    void createOrder_Success() {
        // given
        FranchiseOrderCreateRequestItem requestItem = new FranchiseOrderCreateRequestItem(
                productCode,
                10
        );
        FranchiseOrderCreateRequest request = new FranchiseOrderCreateRequest(
                username,
                phoneNumber,
                deliveryDate,
                deliveryTime,
                address,
                requirement,
                List.of(requestItem)
        );

        Map<String, ProductInfo> productInfoByProductCode = new HashMap<>();
        productInfoByProductCode.put(productCode, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .productName(productName)
                .retailPrice(unitPrice)
                .build());

        // when
        FranchiseOrderCommand response = franchiseOrderService.createOrder(request, orderCode, franchiseId, userId, productInfoByProductCode);

        // then
        assertEquals(FranchiseOrderStatus.PENDING, response.orderStatus());
        assertEquals(totalPrice, response.totalPrice());
    }

    @Test
    @DisplayName("발주 요청 수량 1개 미만 시 예외 발생")
    void createOrder_Failure_INVALID_QUANTITY () {
        // given
        FranchiseOrderCreateRequestItem requestItem = new FranchiseOrderCreateRequestItem(
                productCode,
                0
        );
        FranchiseOrderCreateRequest request = new FranchiseOrderCreateRequest(
                username,
                phoneNumber,
                deliveryDate,
                deliveryTime,
                address,
                requirement,
                List.of(requestItem)
        );

        Map<String, ProductInfo> productInfoByProductCode = new HashMap<>();
        productInfoByProductCode.put(productCode, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .productName(productName)
                .retailPrice(unitPrice)
                .build());

        // when & then
        OrderException exception = assertThrows(OrderException.class, () -> {
            franchiseOrderService.createOrder(request, orderCode, franchiseId, userId, productInfoByProductCode);
        });
        assertEquals(OrderErrorCode.INVALID_QUANTITY, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 대상 발주 목록 조회 - 성공")
    void getAllTargetOrders_Success() {
        // given
        given(franchiseOrderRepository.findAllByFranchiseIdAndUserIdAndOrderStatus(franchiseId, userId, FranchiseOrderStatus.PENDING))
                .willReturn(List.of(franchiseOrder));

        // when
        List<FranchiseReturnTargetResponse> result = franchiseOrderService.getAllTargetOrders(franchiseId, userId, username);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByFranchiseIdAndUserIdAndOrderStatus(franchiseId, userId, FranchiseOrderStatus.PENDING);
        assertEquals(1, result.size());
        assertEquals(orderCode, result.get(0).orderCode());
        assertEquals(username, result.get(0).username());
    }

    @Test
    @DisplayName("본사 발주 접수 처리 - 성공")
    void updateStatus_Accept_Success() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(franchiseOrder));

        // when
        List<HQOrderStatusUpdateResponse> result = franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestAccept);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(FranchiseOrderStatus.ACCEPTED, result.get(0).status());
    }

    @Test
    @DisplayName("본사 발주 반려 처리 - 성공")
    void updateStatus_Reject_Success() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(franchiseOrder));

        // when
        List<HQOrderStatusUpdateResponse> result = franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestReject);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(FranchiseOrderStatus.REJECTED, result.get(0).status());
    }

    @Test
    @DisplayName("존재하지 않는 발주 코드로 상태 수정 시 예외 발생")
    void updateStatus_Failure_ORDER_NOT_FOUND() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestAccept));
        assertEquals(FranchiseOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("배송 중인 발주 접수 처리 시 예외 발생")
    void updateStatus_Accept_Failure_INVALID_STATUS() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(shippingFranchiseOrder));

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestAccept));
        assertEquals(FranchiseOrderErrorCode.ORDER_INVALID_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("배송 중인 발주 반려 처리 시 예외 발생")
    void updateStatus_Reject_Failure_INVALID_STATUS() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(shippingFranchiseOrder));

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestReject));
        assertEquals(FranchiseOrderErrorCode.ORDER_INVALID_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 ID로 발주 코드 조회 - 성공")
    void getOrderCodeByOrderId_Success() {
        // given
        given(franchiseOrderRepository.findById(franchiseOrderId)).willReturn(Optional.of(franchiseOrder));

        // when
        String result = franchiseOrderService.getOrderCodeByOrderId(franchiseOrderId);

        // then
        verify(franchiseOrderRepository, times(1)).findById(franchiseOrderId);
        assertEquals(orderCode, result);
    }

    @Test
    @DisplayName("존재하지 않는 발주 ID로 조회 시 예외 발생")
    void getOrderCodeByOrderId_Failure_ORDER_NOT_FOUND() {
        // given
        given(franchiseOrderRepository.findById(franchiseOrderId)).willReturn(Optional.empty());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.getOrderCodeByOrderId(franchiseOrderId));
        assertEquals(FranchiseOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("여러 발주 ID로 발주 아이템 상품 ID 맵 조회 - 성공")
    void getOrderItemIdsAndProductIdsByOrderIds_Success() {
        // given
        List<Long> orderIds = List.of(franchiseOrderId);
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds))
                .willReturn(List.of(franchiseOrderItem));

        // when
        Map<Long, List<Long>> result = franchiseOrderService.getOrderItemIdsAndProductIdsByOrderIds(orderIds);

        // then
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds);
        assertEquals(List.of(franchiseOrderItemId), result.get(productId));
    }

    @Test
    @DisplayName("여러 발주 ID 조회 시 아이템 없으면 예외 발생")
    void getOrderItemIdsAndProductIdsByOrderIds_Failure_ORDER_ITEM_NOT_FOUND() {
        // given
        List<Long> orderIds = List.of(franchiseOrderId);
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds))
                .willReturn(List.of());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.getOrderItemIdsAndProductIdsByOrderIds(orderIds));
        assertEquals(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("여러 발주 ID로 발주 아이템 커맨드 맵 조회 - 성공")
    void getOrderItemsByOrderIds_Success() {
        // given
        List<Long> orderIds = List.of(franchiseOrderId);
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds))
                .willReturn(List.of(franchiseOrderItem));

        // when
        Map<Long, List<FranchiseOrderItemCommand>> result = franchiseOrderService.getOrderItemsByOrderIds(orderIds);

        // then
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds);
        assertEquals(1, result.get(franchiseOrderId).size());
        assertEquals(franchiseOrderItemId, result.get(franchiseOrderId).get(0).orderItemId());
    }

    @Test
    @DisplayName("단일 발주 ID로 발주 아이템 커맨드 맵 조회 - 성공")
    void getOrderItemsByOrderId_Success() {
        // given
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId))
                .willReturn(List.of(franchiseOrderItem));

        // when
        Map<Long, List<FranchiseOrderItemCommand>> result = franchiseOrderService.getOrderItemsByOrderId(franchiseOrderId);

        // then
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        assertEquals(1, result.get(franchiseOrderId).size());
        assertEquals(franchiseOrderItemId, result.get(franchiseOrderId).get(0).orderItemId());
    }

    @Test
    @DisplayName("단일 발주 ID 조회 시 아이템 없으면 예외 발생")
    void getOrderItemsByOrderId_Failure_ORDER_ITEM_NOT_FOUND() {
        // given
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId))
                .willReturn(List.of());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.getOrderItemsByOrderId(franchiseOrderId));
        assertEquals(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("단일 발주 ID로 상품 ID별 아이템 ID 맵 조회 - 성공")
    void getOrderItemIdsAndProductIdsByOrderId_Success() {
        // given
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId))
                .willReturn(List.of(franchiseOrderItem));

        // when
        Map<Long, List<Long>> result = franchiseOrderService.getOrderItemIdsAndProductIdsByOrderId(franchiseOrderId);

        // then
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId);
        assertEquals(List.of(franchiseOrderItemId), result.get(productId));
    }

    @Test
    @DisplayName("단일 발주 ID 조회 시 아이템 없으면 예외 발생 - 상품 ID 맵")
    void getOrderItemIdsAndProductIdsByOrderId_Failure_ORDER_ITEM_NOT_FOUND() {
        // given
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(franchiseOrderId))
                .willReturn(List.of());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.getOrderItemIdsAndProductIdsByOrderId(franchiseOrderId));
        assertEquals(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 아이템 생성 - 성공")
    void createOrderItems_Success() {
        // given
        FranchiseOrderCreateRequestItem requestItem = new FranchiseOrderCreateRequestItem(productCode, quantity);
        FranchiseOrderCreateRequest request = new FranchiseOrderCreateRequest(
                username, phoneNumber, deliveryDate, deliveryTime, address, requirement, List.of(requestItem));
        Map<String, ProductInfo> productInfoByProductCode = new HashMap<>();
        productInfoByProductCode.put(productCode, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .productName(productName)
                .retailPrice(unitPrice)
                .build());

        given(franchiseOrderRepository.findByOrderCode(orderCode)).willReturn(Optional.of(franchiseOrder));

        // when
        List<FranchiseOrderItemDetailResponse> result = franchiseOrderService.createOrderItems(request, productInfoByProductCode, orderCode);

        // then
        verify(franchiseOrderRepository, times(1)).findByOrderCode(orderCode);
        assertEquals(1, result.size());
        assertEquals(productCode, result.get(0).productCode());
        assertEquals(productName, result.get(0).productName());
        assertEquals(quantity, result.get(0).quantity());
        assertEquals(unitPrice, result.get(0).unitPrice());
        assertEquals(totalPrice, result.get(0).totalPrice());
    }

    @Test
    @DisplayName("존재하지 않는 발주 코드로 아이템 생성 시 예외 발생")
    void createOrderItems_Failure_ORDER_NOT_FOUND() {
        // given
        FranchiseOrderCreateRequestItem requestItem = new FranchiseOrderCreateRequestItem(productCode, quantity);
        FranchiseOrderCreateRequest request = new FranchiseOrderCreateRequest(
                username, phoneNumber, deliveryDate, deliveryTime, address, requirement, List.of(requestItem));
        Map<String, ProductInfo> productInfoByProductCode = new HashMap<>();
        productInfoByProductCode.put(productCode, ProductInfo.builder()
                .productId(productId).productCode(productCode).productName(productName).retailPrice(unitPrice).build());

        given(franchiseOrderRepository.findByOrderCode(orderCode)).willReturn(Optional.empty());

        // when & then
        OrderException exception = assertThrows(OrderException.class, () ->
                franchiseOrderService.createOrderItems(request, productInfoByProductCode, orderCode));
        assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 상품 코드로 아이템 생성 시 예외 발생")
    void createOrderItems_Failure_PRODUCT_NOT_FOUND() {
        // given
        FranchiseOrderCreateRequestItem requestItem = new FranchiseOrderCreateRequestItem("unknownCode", quantity);
        FranchiseOrderCreateRequest request = new FranchiseOrderCreateRequest(
                username, phoneNumber, deliveryDate, deliveryTime, address, requirement, List.of(requestItem));
        Map<String, ProductInfo> emptyProductInfo = new HashMap<>();

        given(franchiseOrderRepository.findByOrderCode(orderCode)).willReturn(Optional.of(franchiseOrder));

        // when & then
        OrderException exception = assertThrows(OrderException.class, () ->
                franchiseOrderService.createOrderItems(request, emptyProductInfo, orderCode));
        assertEquals(OrderErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("여러 발주 ID로 발주 코드 맵 조회 - 성공")
    void getAllOrderCodeByOrderIds_Success() {
        // given
        List<Long> orderIds = List.of(franchiseOrderId);
        given(franchiseOrderRepository.findAllByFranchiseOrderIdInAndDeletedAtIsNull(orderIds))
                .willReturn(List.of(franchiseOrder));

        // when
        Map<Long, String> result = franchiseOrderService.getAllOrderCodeByOrderIds(orderIds);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByFranchiseOrderIdInAndDeletedAtIsNull(orderIds);
        assertEquals(orderCode, result.get(franchiseOrderId));
    }

    @Test
    @DisplayName("여러 발주 ID 조회 시 발주 없으면 예외 발생")
    void getAllOrderCodeByOrderIds_Failure_ORDER_NOT_FOUND() {
        // given
        List<Long> orderIds = List.of(franchiseOrderId);
        given(franchiseOrderRepository.findAllByFranchiseOrderIdInAndDeletedAtIsNull(orderIds))
                .willReturn(List.of());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.getAllOrderCodeByOrderIds(orderIds));
        assertEquals(FranchiseOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 아이템 ID로 상품 ID 맵 조회 - 성공")
    void getProductIdByReturnItemId_Success() {
        // given
        Long returnItemId = 100L;
        Map<Long, Long> orderItemIdByReturnItemId = Map.of(returnItemId, franchiseOrderItemId);
        given(franchiseOrderItemRepository.findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(List.of(franchiseOrderItemId)))
                .willReturn(List.of(franchiseOrderItem));

        // when
        Map<Long, Long> result = franchiseOrderService.getProductIdByReturnItemId(orderItemIdByReturnItemId);

        // then
        assertEquals(productId, result.get(returnItemId));
    }

    @Test
    @DisplayName("반품 아이템 ID 조회 시 아이템 없으면 예외 발생")
    void getProductIdByReturnItemId_Failure_ORDER_ITEM_NOT_FOUND() {
        // given
        Long returnItemId = 100L;
        Map<Long, Long> orderItemIdByReturnItemId = Map.of(returnItemId, franchiseOrderItemId);
        given(franchiseOrderItemRepository.findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(List.of(franchiseOrderItemId)))
                .willReturn(List.of());

        // when & then
        OrderException exception = assertThrows(OrderException.class, () ->
                franchiseOrderService.getProductIdByReturnItemId(orderItemIdByReturnItemId));
        assertEquals(OrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 ID로 발주 상세 조회 - 성공")
    void getOrderByOrderId_Success() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUserIdAndFranchiseOrderIdAndDeletedAtIsNull(franchiseId, userId, franchiseOrderId))
                .willReturn(Optional.of(franchiseOrder));

        // when
        FranchiseOrderDetailCommand result = franchiseOrderService.getOrderByOrderId(franchiseId, userId, franchiseOrderId);

        // then
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndUserIdAndFranchiseOrderIdAndDeletedAtIsNull(franchiseId, userId, franchiseOrderId);
        assertEquals(franchiseOrderId, result.orderId());
        assertEquals(orderCode, result.orderCode());
    }

    @Test
    @DisplayName("존재하지 않는 발주 ID로 조회 시 예외 발생")
    void getOrderByOrderId_Failure_ORDER_NOT_FOUND() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUserIdAndFranchiseOrderIdAndDeletedAtIsNull(franchiseId, userId, franchiseOrderId))
                .willReturn(Optional.empty());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.getOrderByOrderId(franchiseId, userId, franchiseOrderId));
        assertEquals(FranchiseOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 아이템 ID 목록으로 상품 ID 맵 조회 - 성공")
    void getProductIdByOrderItemId_Success() {
        // given
        List<Long> orderItemIds = List.of(franchiseOrderItemId);
        given(franchiseOrderItemRepository.findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(orderItemIds))
                .willReturn(List.of(franchiseOrderItem));

        // when
        Map<Long, Long> result = franchiseOrderService.getProductIdByOrderItemId(orderItemIds);

        // then
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(orderItemIds);
        assertEquals(productId, result.get(franchiseOrderItemId));
    }

    @Test
    @DisplayName("발주 아이템 ID 조회 시 아이템 없으면 예외 발생")
    void getProductIdByOrderItemId_Failure_ORDER_ITEM_NOT_FOUND() {
        // given
        List<Long> orderItemIds = List.of(franchiseOrderItemId);
        given(franchiseOrderItemRepository.findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(orderItemIds))
                .willReturn(List.of());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () ->
                franchiseOrderService.getProductIdByOrderItemId(orderItemIds));
        assertEquals(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

}
