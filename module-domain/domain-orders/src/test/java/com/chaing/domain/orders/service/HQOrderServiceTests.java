package com.chaing.domain.orders.service;

import com.chaing.domain.orders.dto.info.HQOrderInfo;
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

    Long orderId;
    Long orderItemId;
    String orderCode;

    HeadOfficeOrder order;
    HeadOfficeOrderItem orderItem;

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
    void getAllOrderItems_Success() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderIdIn(hqId, List.of(orderId))).willReturn(List.of(orderItem));

        // when
        Map<Long, List<Long>> response = hqOrderService.getAllOrderItems(hqId, List.of(orderId));

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
            hqOrderService.getAllOrderItems(hqId, List.of(orderId));
        });
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderIdIn(hqId, List.of(orderId));
        assertEquals(HQOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
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
    void getOrderItems_Success() {
        // given
        given(orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(hqId, orderId)).willReturn(List.of(orderItem));

        // when
        List<Long> response = hqOrderService.getOrderItems(hqId, orderId);

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
            hqOrderService.getOrderItems(hqId, orderId);
        });
        verify(orderItemRepository, times(1)).findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(hqId, orderId);
        assertEquals(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }
}