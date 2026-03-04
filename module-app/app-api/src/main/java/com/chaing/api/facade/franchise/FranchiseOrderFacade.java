package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.orders.request.FranchiseOrderCreateRequest;
import com.chaing.api.dto.franchise.orders.request.FranchiseOrderUpdateRequest;
import com.chaing.api.dto.franchise.orders.response.FranchiseOrderResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.users.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseOrderFacade {

    private final FranchiseOrderService franchiseOrderService;
    private final UserManagementService userManagementService;
    private final ProductService productService;

    // 가맹점 발주 조회
    public List<FranchiseOrderResponse> getAllOrders(Long userId) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // username
        String username = userManagementService.getUsernameByUserId(userId);

        // Map<orderId, FranchiseOrderCommand>
        Map<Long, FranchiseOrderCommand> orders = franchiseOrderService.getAllOrders(franchiseId, userId);

        // List<orderId>
        List<Long> orderIds = orders.keySet().stream().toList();

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemByOrderId = franchiseOrderService.getOrderItemsByOrderIds(orderIds);

        // Map<orderItemId, orderId>
        Map<Long, Long> orderIdByOrderItemId = orderItemByOrderId.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(item -> Map.entry(entry.getKey(), item.orderItemId())))
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                ));

        // Map<productId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdsByProductId = franchiseOrderService.getOrderItemIdsAndProductIdsByOrderId(orderIds);

        // List<productId>
        List<Long> productIds = orderItemIdsByProductId.keySet().stream().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        return orderItemIdsByProductId.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    Long orderItemId = entry.getValue().get(0);
                    Long orderId = orderIdByOrderItemId.get(orderItemId);

                    FranchiseOrderCommand orderCommand = orders.get(orderId);
                    FranchiseOrderItemCommand orderItemCommand = orderItemByOrderId.get(orderId).get(0);
                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    if (orderCommand == null) {
                        throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
                    }

                    if (orderItemCommand == null) {
                        throw new OrderException(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
                    }

                    if (productInfo == null) {
                        throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return FranchiseOrderResponse.builder()
                            .orderCode(orderCommand.orderCode())
                            .orderStatus(orderCommand.orderStatus())
                            .productCode(productInfo.productCode())
                            .unitPrice(orderItemCommand.unitPrice())
                            .totalPrice(orderCommand.totalPrice())
                            .requestedDate(orderCommand.requestedDate())
                            .receiver(username)
                            .deliveryDate(orderCommand.deliveryDate())
                            .build();
                })
                .toList();
    }

    // 가맹점의 발주 번호에 따른 특정 발주 조회
    public FranchiseOrderResponse getOrder(String username, String orderCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        FranchiseOrder order = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        return FranchiseOrderResponse.from(order);
    }

    // 가맹점의 발주 수정
    @Transactional(rollbackFor = Exception.class)
    public FranchiseOrderResponse updateOrder(String username, String orderCode, FranchiseOrderUpdateRequest request) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 수령인이 user 목록에 없으면 예외 발생

        FranchiseOrder order = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        franchiseOrderService.updateOrder(order, request.toFranchiseOrderUpdateCommand());

        return FranchiseOrderResponse.from(order);
    }

    // 가맹점 발주 취소
    @Transactional(rollbackFor = Exception.class)
    public FranchiseOrderResponse cancelOrder(String username, String orderCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        FranchiseOrder order = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        franchiseOrderService.cancelOrder(order);

        return FranchiseOrderResponse.from(order);
    }

    // 가맹점 발주 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseOrderResponse createOrder(String username, FranchiseOrderCreateRequest request) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 받아온 ProductCode에 따라 제품 정보 가져와서 넘겨줘야 함
        // 이건 임시임. 나중에 Product 엔티티에서 정보 가져오는 걸로 바꿔줘야 함
        List<ProductInfo> productInfos = request.items().stream()
                .map(item -> { return ProductInfo.builder()
                        .productCode(item.productCode())
                        .productId(1L)
                        .unitPrice(BigDecimal.valueOf(5000))
                        .build(); })
                .map(item -> { return ProductInfo.builder().productCode(item.productCode()).build(); })
                .toList();

        FranchiseOrder order = franchiseOrderService.createOrder(franchiseId, username, request.toFranchiseOrderCreateCommand(), productInfos);

        return FranchiseOrderResponse.from(order);
    }
}