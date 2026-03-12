package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.orders.response.FranchiseOrderResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequest;
import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequestItem;
import com.chaing.domain.orders.dto.request.FranchiseOrderStatusUpdateRequest;
import com.chaing.domain.orders.dto.request.FranchiseOrderUpdateRequest;
import com.chaing.domain.orders.dto.response.FranchiseOrderCancelResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderCreateResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderDetailResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderItemDetailResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderStatusShippingPendingResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderUpdateResponse;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
import com.chaing.domain.orders.service.FranchiseOrderCodeGenerator;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.users.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseOrderFacade {

    private final FranchiseOrderService franchiseOrderService;
    private final UserManagementService userManagementService;
    private final ProductService productService;
    private final FranchiseOrderCodeGenerator generator;
    private final FranchiseServiceImpl franchiseService;

    // 가맹점 발주 조회
    public List<FranchiseOrderResponse> getAllOrders(Long userId) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);
        log.info("franchise id: {}", franchiseId);
        log.info("user id: {}", userId);

        // username
        String username = userManagementService.getUsernameByUserId(userId);

        // Map<orderId, FranchiseOrderCommand>
        Map<Long, FranchiseOrderCommand> orders = franchiseOrderService.getAllOrdersByFranchiseIdAndUserId(franchiseId, userId);
        log.info("orders: {}", orders);

        // List<orderId>
        List<Long> orderIds = orders.keySet().stream().toList();

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemByOrderId = franchiseOrderService.getOrderItemsByOrderIds(orderIds);
        log.info("orderItemByOrderId: {}", orderItemByOrderId);

        // List<productId>
        List<Long> productIds = orderItemByOrderId.values().stream()
                .flatMap(List::stream)
                .map(FranchiseOrderItemCommand::productId)
                .distinct()
                .toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        return orders.entrySet().stream()
                .flatMap(entry -> {
                    Long orderId = entry.getKey();
                    FranchiseOrderCommand orderCommand = entry.getValue();
                    List<FranchiseOrderItemCommand> items = orderItemByOrderId.get(orderId);

                    if (items == null || items.isEmpty()) {
                        throw new OrderException(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
                    }

                    return items.stream().map(item -> {
                        ProductInfo productInfo = productInfoByProductId.get(item.productId());

                        if (productInfo == null) {
                            throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                        }

                        return FranchiseOrderResponse.builder()
                                .orderCode(orderCommand.orderCode())
                                .orderStatus(orderCommand.orderStatus())
                                .productCode(productInfo.productCode())
                                .unitPrice(item.unitPrice())
                                .totalPrice(orderCommand.totalPrice())
                                .requestedDate(orderCommand.requestedDate())
                                .receiver(username)
                                .deliveryDate(orderCommand.deliveryDate())
                                .build();
                    });
                })
                .toList();
    }

    // 가맹점의 발주 번호에 따른 특정 발주 조회
    public FranchiseOrderDetailResponse getOrder(Long userId, String orderCode) {
        // userRole 확인
        String userRole = userManagementService.getUserById(userId).getRole().toString();

        Long franchiseId;
        Long orderUserId;
        String username;
        String phoneNumber;

        if (userRole.equals("FRANCHISE")) {
            orderUserId = userId;
            // franchiseId
            franchiseId = userManagementService.getFranchiseIdByUserId(userId);
            // username
            username = userManagementService.getUsernameByUserId(userId);
            // phoneNumber
            phoneNumber = userManagementService.getPhoneNumberByUserId(userId);
        } else if (userRole.equals("HQ")) {
            FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByHQ(orderCode);
            orderUserId = order.userId();

            // franchiseId
            franchiseId = userManagementService.getFranchiseIdByUserId(orderUserId);
            // username
            username = userManagementService.getUsernameByUserId(orderUserId);
            // phoneNumber
            phoneNumber = userManagementService.getPhoneNumberByUserId(orderUserId);
        } else {
            throw new OrderException(OrderErrorCode.UNAUTHORIZED);
        }

        // FranchiseOrderCommand
        FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByOrderCode(franchiseId, orderUserId, orderCode);

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemsByOrderId = franchiseOrderService.getOrderItemsByOrderId(order.orderId());

        // Map<orderItemId, FranchiseOrderItemCommand>>
        Map<Long, FranchiseOrderItemCommand> orderItemCommandByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        FranchiseOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // List<FranchiseOrderItemCommand>
        List<FranchiseOrderItemCommand> orderItems = orderItemsByOrderId.values().stream().flatMap(List::stream).toList();

        // List<productId>
        List<Long> productIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(FranchiseOrderItemCommand::productId)
                .toList();

        // Map<productId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdsByProductId = franchiseOrderService.getOrderItemIdsAndProductIdsByOrderId(order.orderId());

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // List<FranchiseOrderItemDetailResponse>
        List<FranchiseOrderItemDetailResponse> itemResponses = orderItemIdsByProductId.entrySet().stream()
                .flatMap(entry -> {
                    List<Long> orderItemIds = entry.getValue();

                    return orderItemIds.stream()
                            .map(orderItemId -> {
                                FranchiseOrderItemCommand orderItem = orderItemCommandByOrderItemId.get(orderItemId);
                                ProductInfo productInfo = productInfoByProductId.get(entry.getKey());

                                return FranchiseOrderItemDetailResponse.builder()
                                        .productCode(productInfo.productCode())
                                        .productName(productInfo.productName())
                                        .quantity(orderItem.quantity())
                                        .unitPrice(orderItem.unitPrice())
                                        .totalPrice(orderItem.unitPrice().multiply(BigDecimal.valueOf(orderItem.quantity())))
                                        .build();
                            });
                })
                .toList();

        // 반환
        return FranchiseOrderDetailResponse.builder()
                .orderCode(order.orderCode())
                .status(order.orderStatus())
                .requestedDate(order.requestedDate())
                .receiver(username)
                .phoneNumber(phoneNumber)
                .address(order.address())
                .deliveryDate(order.deliveryDate())
                .deliveryTime(order.deliveryTime())
                .items(itemResponses)
                .build();
    }

    // 가맹점의 발주 수정
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseOrderUpdateResponse updateOrder(Long userId, String orderCode, List<FranchiseOrderUpdateRequest> requests) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // FranchiseOrderDetailCommand
        FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByOrderCode(franchiseId, userId, orderCode);

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemsByOrderId = franchiseOrderService.getOrderItemsByOrderId(order.orderId());

        // List<productId>
        List<Long> productIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(FranchiseOrderItemCommand::productId)
                .toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // Map<productCode, ProductInfo>
        Map<String, ProductInfo> productInfoByProductCode = productInfoByProductId.values().stream()
                .collect(Collectors.toMap(
                        ProductInfo::productCode,
                        Function.identity()
                ));

        // Map<productId, FranchiseOrderUpdateRequest>
        Map<Long, FranchiseOrderUpdateRequest> requestByProductId = requests.stream()
                .collect(Collectors.toMap(
                        request -> productInfoByProductCode.get(request.productCode()).productId(),
                        Function.identity()
                ));

        // 발주 수정
        List<FranchiseOrderItemDetailResponse> itemResponses = franchiseOrderService.updateOrder(order.orderId(), requestByProductId, productInfoByProductId);

        return FranchiseOrderUpdateResponse.builder()
                .orderCode(orderCode)
                .cancelReason(order.canceledReason())
                .items(itemResponses)
                .build();
    }

    // 가맹점 발주 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseOrderCancelResponse cancelOrder(Long userId, String orderCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // 취소 및 반환
        return franchiseOrderService.cancelOrder(userId, franchiseId, orderCode);
    }

    // 가맹점 발주 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseOrderCreateResponse createOrder(Long userId, FranchiseOrderCreateRequest request) {
        // 발주 가능한지 재고 확인

        // TODO: 부분 접수되는건 어떻게 할거?

        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // franchiseCode
        String franchiseCode = franchiseService.getById(franchiseId).businessNumber();
        log.info("franchiseId: {}", franchiseId);
        log.info("userId: {}", userId);

        // username1, 2
        String username = userManagementService.getUsernameByUserId(userId);

        // orderCode
        String orderCode = generator.generate(franchiseCode);

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getAllProductInfo();

        // Map<productCode, ProductInfo>
        Map<String, ProductInfo> productInfoByProductCode = productInfoByProductId.values().stream()
                .collect(Collectors.toMap(
                        ProductInfo::productCode,
                        Function.identity()
                ));

        // FranchiseOrderCommand
        FranchiseOrderCommand order = franchiseOrderService.createOrder(request, orderCode, franchiseId, userId, productInfoByProductCode);

        // List<productCode>
        List<String> productCodes = request.items().stream()
                .map(FranchiseOrderCreateRequestItem::productCode)
                .toList();

        // List<FranchiseOrderItemCommand>
        List<FranchiseOrderItemDetailResponse> orderItems = franchiseOrderService.createOrderItems(request, productInfoByProductCode, orderCode);

        // 필요 값
        BigDecimal totalPrice = orderItems.stream()
                .map(FranchiseOrderItemDetailResponse::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // TODO: 정산 생성

        // 반환
        return FranchiseOrderCreateResponse.builder()
                .orderCode(orderCode)
                .orderStatus(order.orderStatus())
                .totalPrice(totalPrice)
                .requestedDate(order.requestedDate())
                .receiver(username)
                .deliveryDate(order.deliveryDate())
                .items(orderItems)
                .build();
    }
}