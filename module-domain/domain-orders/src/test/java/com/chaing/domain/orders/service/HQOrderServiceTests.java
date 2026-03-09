package com.chaing.domain.orders.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.command.HQOrderCancelCommand;
import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.info.HQOrderItemCommand;
import com.chaing.domain.orders.dto.request.FactoryOrderRequest;
import com.chaing.domain.orders.dto.request.HQOrderCreateRequest;
import com.chaing.domain.orders.dto.request.HQOrderItemCreateCommand;
import com.chaing.domain.orders.dto.request.HQOrderItemUpdateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateRequest;
import com.chaing.domain.orders.dto.response.HQOrderForTransitResponse;
import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.enums.HQOrderStatus;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import com.chaing.domain.orders.repository.HeadOfficeOrderItemRepository;
import com.chaing.domain.orders.repository.HeadOfficeOrderRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HQOrderServiceTests {

    @InjectMocks
    private HQOrderService hqOrderService;

    @Mock
    private HeadOfficeOrderRepository orderRepository;

    @Mock
    private HeadOfficeOrderItemRepository orderItemRepository;

    @Mock
    private HQOrderCodeGenerator generator;

    Long userId;
    Long hqId;
    Long orderId;
    Long orderItemId;
    Long productId;
    String orderCode;
    String productCode;
    String hqCode;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;
    LocalDateTime manufactureDate;

    HeadOfficeOrder order;
    HeadOfficeOrder acceptedOrder;
    HeadOfficeOrder canceledOrder;
    HeadOfficeOrderItem orderItem;

    @BeforeEach
    void setUp() {
        userId = 1L;
        hqId = 10L;
        orderId = 1L;
        orderItemId = 10L;
        productId = 100L;
        orderCode = "HQ-20260101-001";
        productCode = "MA-001-1P";
        hqCode = "HQ";
        quantity = 10;
        unitPrice = new BigDecimal("7000.00");
        totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        manufactureDate = LocalDateTime.now().plusDays(7);

        order = HeadOfficeOrder.builder()
                .orderCode(orderCode)
                .userId(userId)
                .manufactureDate(manufactureDate)
                .totalQuantity(quantity)
                .totalAmount(totalPrice)
                .build();
        ReflectionTestUtils.setField(order, "headOfficeOrderId", orderId);

        acceptedOrder = HeadOfficeOrder.builder()
                .orderCode(orderCode)
                .userId(userId)
                .manufactureDate(manufactureDate)
                .orderStatus(HQOrderStatus.ACCEPTED)
                .totalQuantity(quantity)
                .totalAmount(totalPrice)
                .build();

        canceledOrder = HeadOfficeOrder.builder()
                .orderCode(orderCode)
                .userId(userId)
                .manufactureDate(manufactureDate)
                .orderStatus(HQOrderStatus.CANCELED)
                .totalQuantity(quantity)
                .totalAmount(totalPrice)
                .build();

        orderItem = HeadOfficeOrderItem.builder()
                .headOfficeOrder(order)
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
        ReflectionTestUtils.setField(orderItem, "headOfficeOrderItemId", orderItemId);
    }

    @Test
    @DisplayName("본사 발주 전체 조회 - 성공")
    void getAllOrders_Success() {
        // given
        given(orderRepository.findAllByDeletedAtIsNull()).willReturn(List.of(order));

        // when
        Map<Long, HQOrderCommand> result = hqOrderService.getAllOrders();

        // then
        verify(orderRepository, times(1)).findAllByDeletedAtIsNull();
        assertEquals(1, result.size());
        assertEquals(orderCode, result.get(orderId).orderCode());
    }

    @Test
    @DisplayName("발주 없을 때 전체 조회 시 예외 발생")
    void getAllOrders_Failure_ORDER_NOT_FOUND() {
        // given
        given(orderRepository.findAllByDeletedAtIsNull()).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getAllOrders());
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 코드로 특정 발주 조회 - 성공")
    void getOrder_GivenValidOrderCode_ShouldReturnOrder() {
        // given
        given(orderRepository.findByOrderCodeAndDeletedAtIsNull(orderCode)).willReturn(Optional.of(order));

        // when
        HQOrderCommand result = hqOrderService.getOrder(orderCode);

        // then
        verify(orderRepository, times(1)).findByOrderCodeAndDeletedAtIsNull(orderCode);
        assertEquals(orderCode, result.orderCode());
        assertEquals(HQOrderStatus.PENDING, result.status());
    }

    @Test
    @DisplayName("존재하지 않는 발주 코드로 조회 시 예외 발생")
    void getOrder_GivenInvalidOrderCode_ShouldThrowORDER_NOT_FOUND() {
        // given
        given(orderRepository.findByOrderCodeAndDeletedAtIsNull(orderCode)).willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getOrder(orderCode));
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 ID로 발주 제품 productId 목록 조회 - 성공")
    void getOrderItemProductId_GivenValidOrderId_ShouldReturnProductIds() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderId(orderId)).willReturn(List.of(orderItem));

        // when
        List<Long> result = hqOrderService.getOrderItemProductId(hqId, orderId);

        // then
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HeadOfficeOrderId(orderId);
        assertEquals(1, result.size());
        assertEquals(productId, result.get(0));
    }

    @Test
    @DisplayName("발주 제품 없을 때 productId 조회 시 예외 발생")
    void getOrderItemProductId_GivenNoItems_ShouldThrowORDER_ITEM_NOT_FOUND() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderId(orderId)).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getOrderItemProductId(hqId, orderId));
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 제품 수정 - 성공")
    void updateOrderItems_GivenValidRequest_ShouldReturnUpdatedItems() {
        // given
        HQOrderItemUpdateRequest itemRequest = new HQOrderItemUpdateRequest(productCode, 20);
        HQOrderUpdateRequest request = new HQOrderUpdateRequest(manufactureDate, List.of(itemRequest));
        Map<String, ProductInfo> productInfoByProductCode = Map.of(productCode, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .costPrice(unitPrice)
                .build());

        given(orderRepository.findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING))
                .willReturn(Optional.of(order));
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeUserIdAndHeadOfficeOrder_OrderCode(userId, orderCode))
                .willReturn(List.of(orderItem));

        // when
        List<HQOrderItemCommand> result = hqOrderService.updateOrderItems(userId, orderCode, request, productInfoByProductCode);

        // then
        verify(orderRepository, times(1)).findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING);
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HeadOfficeUserIdAndHeadOfficeOrder_OrderCode(userId, orderCode);
        assertEquals(1, result.size());
        assertEquals(productId, result.get(0).productId());
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 발주 수정 시 예외 발생")
    void updateOrderItems_GivenNonPendingOrder_ShouldThrowINVALID_STATUS() {
        // given
        HQOrderItemUpdateRequest itemRequest = new HQOrderItemUpdateRequest(productCode, 20);
        HQOrderUpdateRequest request = new HQOrderUpdateRequest(manufactureDate, List.of(itemRequest));
        Map<String, ProductInfo> productInfoByProductCode = Map.of(productCode, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .costPrice(unitPrice)
                .build());

        given(orderRepository.findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING))
                .willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.updateOrderItems(userId, orderCode, request, productInfoByProductCode));
        assertEquals(HQOrderErrorCode.INVALID_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 제품 없는 발주 수정 시 예외 발생")
    void updateOrderItems_GivenNoItems_ShouldThrowORDER_ITEM_NOT_FOUND() {
        // given
        HQOrderItemUpdateRequest itemRequest = new HQOrderItemUpdateRequest(productCode, 20);
        HQOrderUpdateRequest request = new HQOrderUpdateRequest(manufactureDate, List.of(itemRequest));
        Map<String, ProductInfo> productInfoByProductCode = Map.of(productCode, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .costPrice(unitPrice)
                .build());

        given(orderRepository.findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING))
                .willReturn(Optional.of(order));
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeUserIdAndHeadOfficeOrder_OrderCode(userId, orderCode))
                .willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.updateOrderItems(userId, orderCode, request, productInfoByProductCode));
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 제품 코드로 발주 수정 시 예외 발생")
    void updateOrderItems_GivenUnknownProductCode_ShouldThrowPRODUCT_NOT_FOUND() {
        // given
        HQOrderItemUpdateRequest itemRequest = new HQOrderItemUpdateRequest("unknownCode", 20);
        HQOrderUpdateRequest request = new HQOrderUpdateRequest(manufactureDate, List.of(itemRequest));
        Map<String, ProductInfo> emptyProductInfoMap = Map.of();

        given(orderRepository.findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING))
                .willReturn(Optional.of(order));
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeUserIdAndHeadOfficeOrder_OrderCode(userId, orderCode))
                .willReturn(List.of(orderItem));

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.updateOrderItems(userId, orderCode, request, emptyProductInfoMap));
        assertEquals(HQOrderErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 제조일 수정 - 성공")
    void updateOrder_GivenValidOrderCode_ShouldReturnUpdatedOrder() {
        // given
        LocalDateTime newDate = manufactureDate.plusDays(1);
        given(orderRepository.findByOrderCodeAndDeletedAtIsNull(orderCode)).willReturn(Optional.of(order));

        // when
        HQOrderCommand result = hqOrderService.updateOrder(hqId, orderCode, newDate);

        // then
        verify(orderRepository, times(1)).findByOrderCodeAndDeletedAtIsNull(orderCode);
        assertEquals(orderCode, result.orderCode());
    }

    @Test
    @DisplayName("존재하지 않는 발주 코드로 수정 시 예외 발생")
    void updateOrder_GivenInvalidOrderCode_ShouldThrowORDER_NOT_FOUND() {
        // given
        given(orderRepository.findByOrderCodeAndDeletedAtIsNull(orderCode)).willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.updateOrder(hqId, orderCode, manufactureDate));
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 취소 - 성공")
    void cancel_GivenPendingOrder_ShouldReturnCanceledStatus() {
        // given
        given(orderRepository.findByUserIdAndOrderCodeAndDeletedAtIsNull(userId, orderCode)).willReturn(Optional.of(order));

        // when
        HQOrderCancelCommand result = hqOrderService.cancel(userId, orderCode);

        // then
        verify(orderRepository, times(1)).findByUserIdAndOrderCodeAndDeletedAtIsNull(userId, orderCode);
        assertEquals(HQOrderStatus.CANCELED, result.status());
    }

    @Test
    @DisplayName("존재하지 않는 발주 취소 시 예외 발생")
    void cancel_GivenInvalidOrderCode_ShouldThrowORDER_NOT_FOUND() {
        // given
        given(orderRepository.findByUserIdAndOrderCodeAndDeletedAtIsNull(userId, orderCode)).willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.cancel(userId, orderCode));
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("PENDING이 아닌 발주 취소 시 예외 발생")
    void cancel_GivenNonPendingOrder_ShouldThrowORDER_NOT_PENDING() {
        // given
        given(orderRepository.findByUserIdAndOrderCodeAndDeletedAtIsNull(userId, orderCode)).willReturn(Optional.of(acceptedOrder));

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.cancel(userId, orderCode));
        assertEquals(HQOrderErrorCode.ORDER_NOT_PENDING, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 취소된 발주 재취소 시 예외 발생")
    void cancel_GivenAlreadyCanceledOrder_ShouldThrowORDER_ALREADY_CANCELED() {
        // given
        given(orderRepository.findByUserIdAndOrderCodeAndDeletedAtIsNull(userId, orderCode)).willReturn(Optional.of(canceledOrder));

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.cancel(userId, orderCode));
        assertEquals(HQOrderErrorCode.ORDER_ALREADY_CANCELED, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 생성 - 성공")
    void createOrder_GivenValidRequest_ShouldReturnPendingOrder() {
        // given
        HQOrderItemCreateCommand itemCommand = new HQOrderItemCreateCommand(productCode, quantity);
        HQOrderCreateRequest request = new HQOrderCreateRequest(
                "hq_manager", "010-1234-5678", "테스트 발주", true, manufactureDate, List.of(itemCommand));
        Map<Long, ProductInfo> productInfoByProductId = Map.of(productId, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .costPrice(unitPrice)
                .build());

        given(generator.generate(hqCode)).willReturn(orderCode);

        // when
        HQOrderCommand result = hqOrderService.createOrder(userId, request, hqCode, productInfoByProductId);

        // then
        verify(generator, times(1)).generate(hqCode);
        verify(orderRepository, times(1)).save(any(HeadOfficeOrder.class));
        assertEquals(HQOrderStatus.PENDING, result.status());
    }

    @Test
    @DisplayName("존재하지 않는 제품 코드로 발주 생성 시 예외 발생")
    void createOrder_GivenUnknownProductCode_ShouldThrowPRODUCT_NOT_FOUND() {
        // given
        HQOrderItemCreateCommand itemCommand = new HQOrderItemCreateCommand("unknownCode", quantity);
        HQOrderCreateRequest request = new HQOrderCreateRequest(
                "hq_manager", "010-1234-5678", "테스트 발주", true, manufactureDate, List.of(itemCommand));
        Map<Long, ProductInfo> emptyProductInfoMap = Map.of();

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.createOrder(userId, request, hqCode, emptyProductInfoMap));
        assertEquals(HQOrderErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 제품 생성 - 성공")
    void createOrderItems_GivenValidOrderId_ShouldReturnCreatedItems() {
        // given
        HQOrderItemCreateCommand itemCommand = new HQOrderItemCreateCommand(productCode, quantity);
        Map<Long, ProductInfo> productInfoByProductId = Map.of(productId, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .costPrice(unitPrice)
                .build());

        given(orderRepository.findByHeadOfficeOrderIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

        // when
        List<HQOrderItemCommand> result = hqOrderService.createOrderItems(orderId, productInfoByProductId, List.of(itemCommand));

        // then
        verify(orderRepository, times(1)).findByHeadOfficeOrderIdAndDeletedAtIsNull(orderId);
        assertEquals(1, result.size());
        assertEquals(productId, result.get(0).productId());
        assertEquals(quantity, result.get(0).quantity());
        assertEquals(unitPrice, result.get(0).unitPrice());
    }

    @Test
    @DisplayName("존재하지 않는 발주 ID로 발주 제품 생성 시 예외 발생")
    void createOrderItems_GivenInvalidOrderId_ShouldThrowORDER_NOT_FOUND() {
        // given
        HQOrderItemCreateCommand itemCommand = new HQOrderItemCreateCommand(productCode, quantity);
        Map<Long, ProductInfo> productInfoByProductId = Map.of(productId, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .costPrice(unitPrice)
                .build());

        given(orderRepository.findByHeadOfficeOrderIdAndDeletedAtIsNull(orderId)).willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.createOrderItems(orderId, productInfoByProductId, List.of(itemCommand)));
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("대기 중인 발주 전체 조회 - 성공")
    void getAllPendingOrders_ShouldReturnPendingOrders() {
        // given
        given(orderRepository.findAllByOrderStatus(HQOrderStatus.PENDING)).willReturn(List.of(order));

        // when
        Map<Long, HQOrderCommand> result = hqOrderService.getAllPendingOrders();

        // then
        verify(orderRepository, times(1)).findAllByOrderStatus(HQOrderStatus.PENDING);
        assertEquals(1, result.size());
        assertEquals(orderCode, result.get(orderId).orderCode());
    }

    @Test
    @DisplayName("발주 ID 목록으로 발주 제품 조회 - 성공")
    void getOrderItemIdsByOrderId_GivenValidOrderIds_ShouldReturnItemMap() {
        // given
        List<Long> orderIds = List.of(orderId);
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderIdIn(orderIds)).willReturn(List.of(orderItem));

        // when
        Map<Long, List<com.chaing.domain.orders.dto.command.HQOrderItemCommand>> result =
                hqOrderService.getOrderItemIdsByOrderId(orderIds);

        // then
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HeadOfficeOrderIdIn(orderIds);
        assertEquals(1, result.get(orderId).size());
        assertEquals(orderItemId, result.get(orderId).get(0).orderItemId());
    }

    @Test
    @DisplayName("발주 제품 없을 때 발주 ID 목록 조회 시 예외 발생")
    void getOrderItemIdsByOrderId_GivenNoItems_ShouldThrowORDER_ITEM_NOT_FOUND() {
        // given
        List<Long> orderIds = List.of(orderId);
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderIdIn(orderIds)).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getOrderItemIdsByOrderId(orderIds));
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 제품 ID 목록으로 상품 ID 맵 조회 - 성공")
    void getProductIdsByOrderItemIds_GivenValidIds_ShouldReturnProductIdMap() {
        // given
        List<Long> orderItemIds = List.of(orderItemId);
        given(orderItemRepository.findAllByHeadOfficeOrderItemIdInAndDeletedAtIsNull(orderItemIds)).willReturn(List.of(orderItem));

        // when
        Map<Long, Long> result = hqOrderService.getProductIdsByOrderItemIds(orderItemIds);

        // then
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrderItemIdInAndDeletedAtIsNull(orderItemIds);
        assertEquals(productId, result.get(orderItemId));
    }

    @Test
    @DisplayName("발주 제품 없을 때 상품 ID 맵 조회 시 예외 발생")
    void getProductIdsByOrderItemIds_GivenNoItems_ShouldThrowORDER_ITEM_NOT_FOUND() {
        // given
        List<Long> orderItemIds = List.of(orderItemId);
        given(orderItemRepository.findAllByHeadOfficeOrderItemIdInAndDeletedAtIsNull(orderItemIds)).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getProductIdsByOrderItemIds(orderItemIds));
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("공장 용 전체 발주 조회 - 성공")
    void getAllOrdersByFactory_ShouldReturnAllOrders() {
        // given
        given(orderRepository.findAll()).willReturn(List.of(order));

        // when
        Map<Long, HQOrderCommand> result = hqOrderService.getAllOrdersByFactory();

        // then
        verify(orderRepository, times(1)).findAll();
        assertEquals(1, result.size());
        assertEquals(orderCode, result.get(orderId).orderCode());
    }

    @Test
    @DisplayName("공장 발주 접수 처리 - 성공")
    void updateOrderStatus_GivenAcceptRequest_ShouldReturnAcceptedStatus() {
        // given
        FactoryOrderRequest request = new FactoryOrderRequest(true, List.of(orderCode));
        given(orderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(order));

        // when
        Map<String, HQOrderStatus> result = hqOrderService.updateOrderStatus(request);

        // then
        verify(orderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(HQOrderStatus.ACCEPTED, result.get(orderCode));
    }

    @Test
    @DisplayName("공장 발주 반려 처리 - 성공")
    void updateOrderStatus_GivenRejectRequest_ShouldReturnRejectedStatus() {
        // given
        FactoryOrderRequest request = new FactoryOrderRequest(false, List.of(orderCode));
        given(orderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(order));

        // when
        Map<String, HQOrderStatus> result = hqOrderService.updateOrderStatus(request);

        // then
        verify(orderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(HQOrderStatus.REJECTED, result.get(orderCode));
    }

    @Test
    @DisplayName("존재하지 않는 발주 코드로 상태 변경 시 예외 발생")
    void updateOrderStatus_GivenInvalidOrderCode_ShouldThrowORDER_NOT_FOUND() {
        // given
        FactoryOrderRequest request = new FactoryOrderRequest(true, List.of(orderCode));
        given(orderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.updateOrderStatus(request));
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 발주 접수 처리 시 예외 발생")
    void updateOrderStatus_GivenNonPendingOrder_ShouldThrowORDER_NOT_PENDING() {
        // given
        FactoryOrderRequest request = new FactoryOrderRequest(true, List.of(orderCode));
        given(orderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(acceptedOrder));

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.updateOrderStatus(request));
        assertEquals(HQOrderErrorCode.ORDER_NOT_PENDING, exception.getErrorCode());
    }

    @Test
    @DisplayName("운송을 위한 발주 조회 - 성공")
    void getOrdersForTransit_GivenValidOrderIds_ShouldReturnTransitOrders() {
        // given
        List<Long> orderIds = List.of(orderId);
        HeadOfficeOrder awaitingOrder = HeadOfficeOrder.builder()
                .orderCode(orderCode)
                .userId(userId)
                .manufactureDate(manufactureDate)
                .orderStatus(HQOrderStatus.AWAITING)
                .totalQuantity(quantity)
                .totalAmount(totalPrice)
                .build();
        ReflectionTestUtils.setField(awaitingOrder, "headOfficeOrderId", orderId);
        HeadOfficeOrderItem awaitingItem = HeadOfficeOrderItem.builder()
                .headOfficeOrder(awaitingOrder)
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();

        given(orderItemRepository.findByHeadOfficeOrder_HeadOfficeOrderIdInAndHeadOfficeOrder_OrderStatusAndDeletedAtIsNull(
                orderIds, HQOrderStatus.AWAITING)).willReturn(List.of(awaitingItem));

        // when
        List<HQOrderForTransitResponse> result = hqOrderService.getOrdersForTransit(orderIds);

        // then
        assertEquals(1, result.size());
        assertEquals(orderCode, result.get(0).orderCode());
        assertEquals(1, result.get(0).items().size());
        assertEquals(productId, result.get(0).items().get(0).productId());
    }

    @Test
    @DisplayName("빈 발주 ID 목록으로 운송 발주 조회 시 예외 발생")
    void getOrdersForTransit_GivenEmptyOrderIds_ShouldThrowINVALID_INPUT() {
        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getOrdersForTransit(List.of()));
        assertEquals(HQOrderErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    @DisplayName("운송 대기 제품 없을 때 조회 시 예외 발생")
    void getOrdersForTransit_GivenNoAwaitingItems_ShouldThrowORDER_ITEM_NOT_FOUND() {
        // given
        List<Long> orderIds = List.of(orderId);
        given(orderItemRepository.findByHeadOfficeOrder_HeadOfficeOrderIdInAndHeadOfficeOrder_OrderStatusAndDeletedAtIsNull(
                orderIds, HQOrderStatus.AWAITING)).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getOrdersForTransit(orderIds));
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("요청한 발주보다 조회된 발주가 적을 때 예외 발생")
    void getOrdersForTransit_GivenPartialOrders_ShouldThrowORDER_NOT_FOUND() {
        // given
        List<Long> orderIds = List.of(orderId, 2L);
        HeadOfficeOrder awaitingOrder = HeadOfficeOrder.builder()
                .orderCode(orderCode)
                .userId(userId)
                .manufactureDate(manufactureDate)
                .orderStatus(HQOrderStatus.AWAITING)
                .totalQuantity(quantity)
                .totalAmount(totalPrice)
                .build();
        ReflectionTestUtils.setField(awaitingOrder, "headOfficeOrderId", orderId);
        HeadOfficeOrderItem awaitingItem = HeadOfficeOrderItem.builder()
                .headOfficeOrder(awaitingOrder)
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();

        given(orderItemRepository.findByHeadOfficeOrder_HeadOfficeOrderIdInAndHeadOfficeOrder_OrderStatusAndDeletedAtIsNull(
                orderIds, HQOrderStatus.AWAITING)).willReturn(List.of(awaitingItem));

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getOrdersForTransit(orderIds));
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 ID로 발주 제품 맵 조회 - 성공")
    void getOrderItemsByOrderId_GivenValidOrderId_ShouldReturnItemMap() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderId(orderId)).willReturn(List.of(orderItem));

        // when
        Map<Long, List<HQOrderItemCommand>> result = hqOrderService.getOrderItemsByOrderId(orderId);

        // then
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HeadOfficeOrderId(orderId);
        assertEquals(1, result.get(orderId).size());
        assertEquals(orderItemId, result.get(orderId).get(0).orderItemId());
    }

    @Test
    @DisplayName("발주 제품 없을 때 발주 ID 조회 시 예외 발생")
    void getOrderItemsByOrderId_GivenNoItems_ShouldThrowORDER_ITEM_NOT_FOUND() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderId(orderId)).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getOrderItemsByOrderId(orderId));
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("userId와 발주 코드로 PENDING 발주 조회 - 성공")
    void getOrderByUserIdAndOrderCodeAndPending_GivenPendingOrder_ShouldReturnOrder() {
        // given
        given(orderRepository.findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING))
                .willReturn(Optional.of(order));

        // when
        HQOrderCommand result = hqOrderService.getOrderByUserIdAndOrderCodeAndPending(userId, orderCode);

        // then
        verify(orderRepository, times(1)).findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING);
        assertEquals(orderCode, result.orderCode());
        assertEquals(HQOrderStatus.PENDING, result.status());
    }

    @Test
    @DisplayName("PENDING이 아닌 발주를 userId와 발주 코드로 조회 시 예외 발생")
    void getOrderByUserIdAndOrderCodeAndPending_GivenNonPendingOrder_ShouldThrowINVALID_STATUS() {
        // given
        given(orderRepository.findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING))
                .willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () ->
                hqOrderService.getOrderByUserIdAndOrderCodeAndPending(userId, orderCode));
        assertEquals(HQOrderErrorCode.INVALID_STATUS, exception.getErrorCode());
    }
}