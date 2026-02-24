package com.chaing.domain.orders.service;

import com.chaing.domain.orders.dto.command.FranchiseOrderCreateCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderUpdateCommand;
import com.chaing.domain.orders.dto.info.FranchiseOrderCreateInfo;
import com.chaing.domain.orders.dto.info.FranchiseOrderItemInfo;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.FranchiseOrderErrorCode;
import com.chaing.domain.orders.exception.FranchiseOrderException;
import com.chaing.domain.orders.repository.FranchiseOrderItemRepository;
import com.chaing.domain.orders.repository.FranchiseOrderRepository;
import com.chaing.domain.orders.support.ProductInfo;
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
class FranchiseOrderServiceTests {
    @InjectMocks
    private FranchiseOrderService franchiseOrderService;

    @Mock
    private FranchiseOrderRepository franchiseOrderRepository;

    @Mock
    private FranchiseOrderItemRepository franchiseOrderItemRepository;

    Long franchiseId;
    String username;

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

    Long franchiseOrderItemId;
    Long productId;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;

    FranchiseOrder franchiseOrder;
    FranchiseOrderItem franchiseOrderItem;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;
        username = "test";

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

        franchiseOrder = FranchiseOrder.builder()
                .franchiseOrderId(franchiseOrderId)
                .franchiseId(franchiseId)
                .orderCode(orderCode)
                .phoneNumber(phoneNumber)
                .address(address)
                .username(username)
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
    }

    @Test
    @DisplayName("FranchiseOrderItem getter 및 기본 생성자 테스트")
    void testGettersAndNoArgsConstructor() {
        // NoArgsConstructor 커버
        FranchiseOrderItem item = FranchiseOrderItem.builder().build();
        // 필드 값 설정 (Reflection으로 protected 접근)
        ReflectionTestUtils.setField(item, "franchiseOrderItemId", 1L);
        ReflectionTestUtils.setField(item, "productId", 100L);
        FranchiseOrder order = FranchiseOrder.builder().build();
        ReflectionTestUtils.setField(item, "franchiseOrder", order);
        // 미커버 getter 호출
        assertEquals(1L, item.getFranchiseOrderItemId());
        assertEquals(100L, item.getProductId());
        assertEquals(order, item.getFranchiseOrder());
    }

    @Test
    @DisplayName("가맹점 발주 목록 조회 - 성공")
    void getAllOrders_Success() {
        // given
        given(franchiseOrderRepository.findAllByFranchiseIdAndUsername(franchiseId, username)).willReturn(List.of(franchiseOrder));

        // when
        List<FranchiseOrder> orders = franchiseOrderService.getAllOrders(franchiseId, username);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByFranchiseIdAndUsername(franchiseId, username);
        assertEquals(1, orders.size());
        assertEquals(1L, orders.get(0).getFranchiseOrderId());
    }

    @Test
    @DisplayName("가맹점 특정 발주 조회 - 성공")
    void getOrder_Success() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUsernameAndOrderCode(franchiseId, username, orderCode)).willReturn(Optional.of(franchiseOrder));

        // when
        FranchiseOrder result = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        // then
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndUsernameAndOrderCode(franchiseId, username, orderCode);
        assertEquals(franchiseOrder, result);
    }

    @Test
    @DisplayName("존재하지 않는 발주 코드로 조회 시 예외 발생")
    void getOrder_Failure_ORDER_NOT_FOUND() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndUsernameAndOrderCode(franchiseId, username, orderCode)).willReturn(Optional.empty());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> franchiseOrderService.getOrder(franchiseId, username, orderCode));
        assertEquals(FranchiseOrderException.class, exception.getClass());
        assertEquals(FranchiseOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("가맹점 발주 수정 - 성공")
    void updateOrder_Success() {
        // given
        FranchiseOrderItemInfo info = new FranchiseOrderItemInfo(
                10L,
                20,
                BigDecimal.valueOf(2000)
        );

        FranchiseOrderUpdateCommand command = new FranchiseOrderUpdateCommand(
                "test",
                "phoneNumber",
                "address",
                "time",
                "requirement",
                List.of(info)
        );

        given(franchiseOrderItemRepository.findByFranchiseOrder_FranchiseOrderIdAndSerialCode(franchiseOrderId, productId)).willReturn(Optional.of(franchiseOrderItem));

        // when
        franchiseOrderService.updateOrder(franchiseOrder, command);

        // then
        verify(franchiseOrderItemRepository, times(command.items().size())).findByFranchiseOrder_FranchiseOrderIdAndSerialCode(franchiseOrderId, productId);
        assertEquals(20, franchiseOrderItem.getQuantity());
        assertEquals(BigDecimal.valueOf(2000), franchiseOrderItem.getUnitPrice());
        assertEquals("phoneNumber", franchiseOrder.getPhoneNumber());
    }

    @Test
    @DisplayName("발주 상태가 PENDING이 아닐 때 수정 시도 시 예외 발생")
    void updateOrder_Failure_ORDER_INVALID_STATUS() {
        // given
        FranchiseOrder order = FranchiseOrder.builder()
                .orderStatus(FranchiseOrderStatus.SHIPPING)
                .build();
        ReflectionTestUtils.setField(order, "franchiseOrderId", 200L);

        FranchiseOrderItemInfo info = new FranchiseOrderItemInfo(
                10L,
                20,
                BigDecimal.valueOf(2000)
        );

        FranchiseOrderUpdateCommand command = new FranchiseOrderUpdateCommand(
                "test",
                "phoneNumber",
                "address",
                "time",
                "requirement",
                List.of(info)
        );

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> {
            franchiseOrderService.updateOrder(order, command);
        });
        assertEquals(FranchiseOrderErrorCode.ORDER_INVALID_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 제품 주문 시 예외 발생")
    void updateOrder_Failure_ORDER_NOT_FOUND () {
        // given
        FranchiseOrderItemInfo info = new FranchiseOrderItemInfo(
                1000L,
                20,
                BigDecimal.valueOf(2000)
        );

        FranchiseOrderUpdateCommand command = new FranchiseOrderUpdateCommand(
                "test",
                "phoneNumber",
                "address",
                "time",
                "requirement",
                List.of(info)
        );

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> {
            franchiseOrderService.updateOrder(franchiseOrder, command);
        });
        assertEquals(FranchiseOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("가맹점 발주 취소")
    void cancelOrder_Success() {
        // given

        // when
        franchiseOrderService.cancelOrder(franchiseOrder);

        // then
        assertEquals(FranchiseOrderStatus.CANCELED, franchiseOrder.getOrderStatus());
    }

    @Test
    @DisplayName("가맹점 발주 생성 - 성공")
    void createOrder_Success() {
        // given
        FranchiseOrderCreateInfo info = new FranchiseOrderCreateInfo(
                "ProductCode",
                20
        );

        FranchiseOrderCreateCommand command = new FranchiseOrderCreateCommand(
                "username",
                "phoneNumber",
                LocalDateTime.now(),
                "11:00",
                "address",
                "requirement",
                List.of(info)
        );

        ProductInfo productInfo = ProductInfo.builder()
                .productCode("ProductCode")
                .productId(1L)
                .unitPrice(BigDecimal.valueOf(5000))
                .build();

        // when
        FranchiseOrder order = franchiseOrderService.createOrder(franchiseId, username, command, List.of(productInfo));

        // then
        assertEquals(20, order.getTotalQuantity());
        assertEquals(BigDecimal.valueOf(100000), order.getTotalAmount());
        assertEquals("phoneNumber", order.getPhoneNumber());
    }

    @Test
    @DisplayName("등록되지 않은 제품에 대한 발주 생성 시 예외 발생")
    void createOrder_Failure_ORDER_NOT_FOUND() {
        // given
        FranchiseOrderCreateInfo info = new FranchiseOrderCreateInfo(
                "incorrectProductCode",
                20
        );

        FranchiseOrderCreateCommand command = new FranchiseOrderCreateCommand(
                "username",
                "phoneNumber",
                LocalDateTime.now(),
                "11:00",
                "address",
                "requirement",
                List.of(info)
        );

        ProductInfo productInfo = ProductInfo.builder()
                .productCode("ProductCode")
                .productId(1L)
                .unitPrice(BigDecimal.valueOf(5000))
                .build();

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> {
            franchiseOrderService.createOrder(franchiseId, username, command, List.of(productInfo));
        });
        assertEquals(FranchiseOrderErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }


}