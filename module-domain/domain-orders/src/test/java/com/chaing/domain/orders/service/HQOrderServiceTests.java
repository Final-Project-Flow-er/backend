package com.chaing.domain.orders.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.info.HQOrderInfo;
import com.chaing.domain.orders.dto.info.HQOrderItemInfo;
import com.chaing.domain.orders.dto.reqeust.HQOrderItemUpdateRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    Long hqId;
    String username;
    String phoneNumber;
    LocalDateTime manufactureDate;
    String description;
    String storedDate;
    Integer totalQuantity;
    BigDecimal totalAmount;

    Long productId;
    String productCode;
    String productName;
    Integer quantity;
    BigDecimal unitPrice;

    Long newProductId;
    Integer newQuantity;

    Long orderId;
    Long orderItemId;
    String orderCode;

    HeadOfficeOrder order;
    HeadOfficeOrderItem orderItem;

    HQOrderItemUpdateRequest hqOrderItemUpdateRequest;

    Map<Long, ProductInfo> productInfoByProductId;

    @BeforeEach
    public void setUp() {
        hqId = 1L;
        username = "username";
        phoneNumber = "phoneNumber";
        manufactureDate = LocalDateTime.now();
        description = "description";
        storedDate = "storedDate";
        totalQuantity = 10;
        totalAmount = BigDecimal.valueOf(50000);

        productId = 1L;
        productCode = "productCode";
        productName = "productName";

        orderItemId = 1L;
        quantity = 1;
        unitPrice = BigDecimal.valueOf(5000);

        orderId = 10L;
        orderCode = "orderCode";

        order = HeadOfficeOrder.builder()
                .hqId(hqId)
                .username(username)
                .phoneNumber(phoneNumber)
                .manufactureDate(manufactureDate)
                .description(description)
                .storedDate(storedDate)
                .orderStatus(HQOrderStatus.PENDING)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .isRegular(true)
                .orderCode(orderCode)
                .build();
        ReflectionTestUtils.setField(order, "headOfficeOrderId", orderId);

        orderItem = HeadOfficeOrderItem.builder()
                .headOfficeOrder(order)
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
        ReflectionTestUtils.setField(orderItem, "headOfficeOrderItemId", orderItemId);

        newProductId = 20L;
        newQuantity = 1000;
        hqOrderItemUpdateRequest = new HQOrderItemUpdateRequest(
                newProductId,
                newQuantity
        );

        productInfoByProductId = new HashMap<>();
        productInfoByProductId.put(1L, ProductInfo.builder()
                        .productId(productId)
                        .productCode(productCode)
                        .productName(productName)
                        .retailPrice(BigDecimal.valueOf(5000))
                        .costPrice(BigDecimal.valueOf(3000))
                        .tradePrice(BigDecimal.valueOf(10000))
                .build());
        productInfoByProductId.put(20L, ProductInfo.builder()
                .productId(productId)
                .productCode(productCode)
                .productName(productName)
                .retailPrice(BigDecimal.valueOf(5000))
                .costPrice(BigDecimal.valueOf(3000))
                .tradePrice(BigDecimal.valueOf(10000))
                .build());
    }

    @Test
    @DisplayName("발주 정보 조회 - 성공")
    void getAllOrders() {
        // given
        given(orderRepository.findAllByHqIdAndUsername(hqId, username)).willReturn(List.of(order));

        // when
        Map<Long, HQOrderInfo> response = hqOrderService.getAllOrders(hqId, username);

        // then
        verify(orderRepository, times(1)).findAllByHqIdAndUsername(hqId, username);
        assertEquals(orderId, response.get(orderId).orderId());
    }

    @Test
    @DisplayName("발주 제품 정보 조회 - 성공")
    void getAllOrderItemProductId_Success() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderIdIn(hqId, List.of(orderId))).willReturn(List.of(orderItem));

        // when
        Map<Long, List<Long>> response = hqOrderService.getAllOrderItemProductId(hqId, List.of(orderId));

        // then
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderIdIn(hqId, List.of(orderId));
        assertEquals(1L, response.get(orderId).get(0));
    }

    @Test
    @DisplayName("잘못된 orderId로 headOfficeOrder 조회 시 예외 발생")
    void getAllOrderItems_Failure_ORDER_NOT_FOUND() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderIdIn(hqId, List.of(orderId))).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.getAllOrderItemProductId(hqId, List.of(orderId));
        });
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderIdIn(hqId, List.of(orderId));
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("특정 발주 정보 조회 - 성공")
    void getOrder_Success() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.of(order));

        // when
        HQOrderInfo response = hqOrderService.getOrder(hqId, orderCode);

        // then
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        assertEquals(orderId, response.orderId());
        assertEquals(orderCode, response.orderCode());
    }

    @Test
    @DisplayName("잘못된 orderCode로 발주 정보 조회 시 예외 발생")
    void getOrder_Failure_ORDER_NOT_FOUND() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.getOrder(hqId, orderCode);
        });
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 제품 productId 조회 - 성공")
    void getOrderItemProductId_Success() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(hqId, orderId)).willReturn(List.of(orderItem));

        // when
        List<Long> response = hqOrderService.getOrderItemProductId(hqId, orderId);

        // then
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(hqId, orderId);
        assertEquals(orderItemId, response.get(0));
    }

    @Test
    @DisplayName("잘못된 orderId로 발주 제품 조회 시 예외 발생")
    void getOrderItems_Failure_ORDER_ITEM_NOT_FOUND() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(hqId, orderId)).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.getOrderItemProductId(hqId, orderId);
        });
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(hqId, orderId);
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 제품 수정 - 성공")
    void updateOrderItems_Success() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.of(order));
        given(orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(hqId, orderCode)).willReturn(List.of(orderItem));

        // when
        List<HQOrderItemInfo> response = hqOrderService.updateOrderItems(hqId, orderCode, List.of(hqOrderItemUpdateRequest), productInfoByProductId);

        // then
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(hqId, orderCode);
        assertTrue(response.stream()
                .anyMatch(info -> info.productId().equals(newProductId)));
        assertTrue(response.stream()
                .anyMatch(info -> info.quantity().equals(newQuantity)));
        assertFalse(response.stream()
                .noneMatch(info -> info.productId().equals(newProductId)));
        assertFalse(response.stream()
                .noneMatch(info -> info.quantity().equals(newQuantity)));
    }

    @Test
    @DisplayName("잘못된 값으로 발주 조회 시 예외 발생")
    void updateOrderItems_Failure_ORDER_NOT_FOUND() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.updateOrderItems(hqId, orderCode, List.of(hqOrderItemUpdateRequest), productInfoByProductId);
        });
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        verify(orderItemRepository, times(0)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(hqId, orderCode);
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("잘못된 값으로 발주 제품 조회 시 예외 발생")
    void updateOrderItems_Failure_ORDER_ITEM_NOT_FOUND() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.of(order));
        given(orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(hqId, orderCode)).willReturn(List.of());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.updateOrderItems(hqId, orderCode, List.of(hqOrderItemUpdateRequest), productInfoByProductId);
        });
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(hqId, orderCode);
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 정보 수정 - 성공")
    void updateOrder_Success() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.of(order));
        LocalDateTime manufactureDate = LocalDateTime.of(2026, 2, 20, 10, 0);

        // when
        HQOrderInfo response = hqOrderService.updateOrder(hqId, orderCode, manufactureDate);

        // then
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        assertEquals(manufactureDate, response.manufacturedDate());
    }

    @Test
    @DisplayName("잘못된 정보로 발주 조회 시 예외 발생")
    void updateOrder_Failure_ORDER_NOT_FOUND() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.updateOrder(hqId, orderCode, manufactureDate);
        });
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 취소 - 성공")
    void cancelOrder_Success() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.of(order));

        // when
        Map<String, HQOrderStatus> response = hqOrderService.cancel(hqId, orderCode);

        // then
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        assertEquals(HQOrderStatus.CANCELED, response.values().iterator().next());
        assertEquals(orderCode, response.keySet().iterator().next());
    }

    @Test
    @DisplayName("발주 상태가 CANCELLED일 때 취소 시도 시 예외 발생")
    void cancelOrder_Failure_ORDER_ALREADY_CANCELLED() {
        // given
        HeadOfficeOrder cancelledOrder = HeadOfficeOrder.builder()
                .orderStatus(HQOrderStatus.CANCELED)
                .build();
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.of(cancelledOrder));

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.cancel(hqId, orderCode);
        });
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        assertEquals(HQOrderErrorCode.ORDER_ALREADY_CANCELED, exception.getErrorCode());
    }

    @Test
    @DisplayName("발주 상태가 PENDING이 아닐 때 취소 요청 시 예외 발생")
    void cancelOrder_Failure_ORDER_NOT_PENDING() {
        // given
        HeadOfficeOrder shippingOrder = HeadOfficeOrder.builder()
                .orderStatus(HQOrderStatus.SHIPPING)
                .build();
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.of(shippingOrder));

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.cancel(hqId, orderCode);
        });
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        assertEquals(HQOrderErrorCode.ORDER_NOT_PENDING, exception.getErrorCode());
    }

    @Test
    @DisplayName("잘못된 값으로 발주 조회 시 예외 발생")
    void cancelOrder_Failure_ORDER_NOT_FOUND() {
        // given
        given(orderRepository.findByHqIdAndOrderCode(hqId, orderCode)).willReturn(Optional.empty());

        // when & then
        HQOrderException exception = assertThrows(HQOrderException.class, () -> {
            hqOrderService.cancel(hqId, orderCode);
        });
        verify(orderRepository, times(1)).findByHqIdAndOrderCode(hqId, orderCode);
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }
}