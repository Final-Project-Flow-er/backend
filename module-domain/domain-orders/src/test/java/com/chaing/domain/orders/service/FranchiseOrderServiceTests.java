package com.chaing.domain.orders.service;

import com.chaing.core.dto.returns.request.OrderItemIdAndSerialCode;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.domain.orders.dto.command.FranchiseOrderCreateCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderUpdateCommand;
import com.chaing.domain.orders.dto.info.FranchiseOrderCreateInfo;
import com.chaing.domain.orders.dto.info.FranchiseOrderItemInfo;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
import com.chaing.domain.orders.dto.response.HQOrderStatusUpdateResponse;
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

    FranchiseOrder franchiseOrder;
    FranchiseOrderItem franchiseOrderItem;

    FranchiseOrder shippingFranchiseOrder;

    HQOrderUpdateStatusRequest hqOrderUpdateStatusRequestAccept;
    HQOrderUpdateStatusRequest hqOrderUpdateStatusRequestReject;

    @BeforeEach
    void setUp() {
        franchiseId = 1L;
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
                .serialCode(serialCode)
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
    @DisplayName("FranchiseOrderItem getter 및 기본 생성자 테스트")
    void testGettersAndNoArgsConstructor() {
        // NoArgsConstructor 커버
        FranchiseOrderItem item = FranchiseOrderItem.builder()
                .serialCode(serialCode)
                .build();
        // 필드 값 설정 (Reflection으로 protected 접근)
        ReflectionTestUtils.setField(item, "franchiseOrderItemId", 1L);
        FranchiseOrder order = FranchiseOrder.builder().build();
        ReflectionTestUtils.setField(item, "franchiseOrder", order);
        // 미커버 getter 호출
        assertEquals(1L, item.getFranchiseOrderItemId());
        assertEquals(serialCode, item.getSerialCode());
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

    // 발주 번호에 따른 발주 정보 반환

    @Test
    @DisplayName("가맹점 발주 수정 - 성공")
    void updateOrder_Success() {
        // given
        FranchiseOrderItemInfo info = new FranchiseOrderItemInfo(
                serialCode,
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

        given(franchiseOrderItemRepository.findByFranchiseOrder_FranchiseOrderIdAndSerialCode(franchiseOrderId, serialCode)).willReturn(Optional.of(franchiseOrderItem));

        // when
        franchiseOrderService.updateOrder(franchiseOrder, command);

        // then
        verify(franchiseOrderItemRepository, times(command.items().size())).findByFranchiseOrder_FranchiseOrderIdAndSerialCode(franchiseOrderId, serialCode);
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
                serialCode,
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
                serialCode,
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

    @Test
    @DisplayName("franchiseOrderId에 대한 returnCode 반환 - 성공")
    void getAllOrderCode_Success() {
        // given
        given(franchiseOrderRepository.findAllByFranchiseOrderIdIn(List.of(franchiseOrderId))).willReturn(List.of(franchiseOrder));

        // when
        Map<Long, String> response = franchiseOrderService.getAllOrderCode(List.of(franchiseOrderId));

        // then
        verify(franchiseOrderRepository, times(1)).findAllByFranchiseOrderIdIn(List.of(franchiseOrderId));
        assertEquals(orderCode, response.get(franchiseOrderId));
    }

    @Test
    @DisplayName("franchiseOrderItemId에 대한 serialCode Map 반환 - 성공")
    void getSerialCodes_Success() {
        // given
        given(franchiseOrderItemRepository.findAllByFranchiseOrderItemIdIn(List.of(franchiseOrderItemId))).willReturn(List.of(franchiseOrderItem));

        // when
        List<OrderItemIdAndSerialCode> responses = franchiseOrderService.getSerialCodes(List.of(franchiseOrderItemId));

        // then
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrderItemIdIn(List.of(franchiseOrderItemId));
        assertEquals(serialCode, responses.get(0).serialCode());
    }

    @Test
    @DisplayName("orderItemId에 대한 serialCode List 반환 - 성공")
    void getSerialCodeList_Success() {
        // given
        given(franchiseOrderItemRepository.findAllByFranchiseOrderItemIdIn(List.of(franchiseOrderItemId))).willReturn(List.of(franchiseOrderItem));

        // when
        List<String> responses = franchiseOrderService.getSerialCodeList(List.of(franchiseOrderItemId));

        // then
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrderItemIdIn(List.of(franchiseOrderItemId));
        assertEquals(serialCode, responses.get(0));
    }

    @Test
    @DisplayName("orderId, franchiseId에 대한 orderCode 반환 - 성공")
    void getOrderCode_Success() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndFranchiseOrderId(1L, 10L)).willReturn(Optional.of(franchiseOrder));

        // when
        String response = franchiseOrderService.getOrderCode(1L, 10L);

        // then
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndFranchiseOrderId(franchiseId, franchiseOrderItemId);
        assertEquals(orderCode, response);
    }

    @Test
    @DisplayName("잘못된 orderId, franchiseId로 발주 조회 시 예외 발생")
    void getOrderCode_Failure_ORDER_NOT_FOUND() {
        // given
        given(franchiseOrderRepository.findByFranchiseIdAndFranchiseOrderId(1L, 10L)).willReturn(Optional.empty());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> {
            franchiseOrderService.getOrderCode(1L, 10L);
        });
        verify(franchiseOrderRepository, times(1)).findByFranchiseIdAndFranchiseOrderId(1L, 10L);
        assertEquals(FranchiseOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("serialCode에 대한 orderItemId 반환 - 성공")
    void getOrderItemId_Success() {
        // given
        given(franchiseOrderItemRepository.findBySerialCode(serialCode)).willReturn(Optional.of(franchiseOrderItem));

        // when
        Long response = franchiseOrderService.getOrderItemId(serialCode);

        // then
        verify(franchiseOrderItemRepository, times(1)).findBySerialCode(serialCode);
        assertEquals(franchiseOrderItemId, response);
    }

    @Test
    @DisplayName("잘못된 serialCode로 발주 제품 조회 시 예외 발생")
    void getOrderItemId_Failure_ORDER_ITEM_NOT_FOUND() {
        // given
        given(franchiseOrderItemRepository.findBySerialCode(serialCode)).willReturn(Optional.empty());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> {
            franchiseOrderService.getOrderItemId(serialCode);
        });
        verify(franchiseOrderItemRepository, times(1)).findBySerialCode(serialCode);
        assertEquals(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("반품 대상이 되는 발주 반환 - 성공")
    void getAllTargetOrders_Success() {
        // given
        given(franchiseOrderRepository.findAllByFranchiseIdAndOrderStatus(franchiseId, FranchiseOrderStatus.PENDING)).willReturn(List.of(franchiseOrder));

        // when
        List<FranchiseReturnTargetResponse> responses = franchiseOrderService.getAllTargetOrders(franchiseId, username);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByFranchiseIdAndOrderStatus(franchiseId, FranchiseOrderStatus.PENDING);
        assertEquals(orderCode, responses.get(0).orderCode());
        assertEquals(username, responses.get(0).username());
    }

    // orderCode로 해당 orderItem serialCode 반환 - List
    @Test
    @DisplayName("orderCode로 해당 orderItem serialCode List 반환 - 성공")
    void getSerialCodesByOrderCode_Success() {
        // given
        given(franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseIdAndFranchiseOrder_OrderCode(franchiseId, orderCode)).willReturn(List.of(franchiseOrderItem));

        // when
        List<String> responses = franchiseOrderService.getSerialCodesByOrderCode(franchiseId, orderCode);

        // then
        verify(franchiseOrderItemRepository, times(1)).findAllByFranchiseOrder_FranchiseIdAndFranchiseOrder_OrderCode(franchiseId, orderCode);
        assertEquals(serialCode, responses.get(0));
    }

    @Test
    @DisplayName("본사에서의 가맹점의 발주 접수 -  성공")
    void updateOrderStatus_Accept_Success() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(franchiseOrder));

        // when
        List<HQOrderStatusUpdateResponse> responses = franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestAccept);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(orderCode, responses.get(0).orderCode());
        assertEquals(FranchiseOrderStatus.ACCEPTED, responses.get(0).status());
    }

    @Test
    @DisplayName("본사에서의 가맹점의 발주 반려 -  성공")
    void updateOrderStatus_Reject_Success() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(franchiseOrder));

        // when
        List<HQOrderStatusUpdateResponse> responses = franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestReject);

        // then
        verify(franchiseOrderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(orderCode, responses.get(0).orderCode());
        assertEquals(FranchiseOrderStatus.REJECTED, responses.get(0).status());
    }

    @Test
    @DisplayName("잘못된 값으로 발주 조회 시 예외 발생")
    void updateOrderStatus_Failure_ORDER_NOT_FOUND() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of());

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> {
            franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestAccept);
        });
        verify(franchiseOrderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(FranchiseOrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("가맹점 발주 접수 시 상태가 PENDING이 아닐 경우 예외 발생")
    void updateOrderStatus_Accept_Failure_ORDER_INVALID_STATUS() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(shippingFranchiseOrder));

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> {
            franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestAccept);
        });
        verify(franchiseOrderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(FranchiseOrderErrorCode.ORDER_INVALID_STATUS, exception.getErrorCode());
    }

    @Test
    @DisplayName("가맹점 발주 반려 시 상태가 PENDING이 아닐 경우 예외 발생")
    void updateOrderStatus_Reject_Failure_ORDER_INVALID_STATUS() {
        // given
        given(franchiseOrderRepository.findAllByOrderCodeIn(List.of(orderCode))).willReturn(List.of(shippingFranchiseOrder));

        // when & then
        FranchiseOrderException exception = assertThrows(FranchiseOrderException.class, () -> {
            franchiseOrderService.updateStatus(hqOrderUpdateStatusRequestReject);
        });
        verify(franchiseOrderRepository, times(1)).findAllByOrderCodeIn(List.of(orderCode));
        assertEquals(FranchiseOrderErrorCode.ORDER_INVALID_STATUS, exception.getErrorCode());
    }
}