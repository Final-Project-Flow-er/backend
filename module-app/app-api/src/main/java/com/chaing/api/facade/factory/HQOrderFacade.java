package com.chaing.api.facade.factory;

import com.chaing.domain.businessunits.service.BusinessUnitService;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.HQOrderCreateRequest;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.info.HQOrderInfo;
import com.chaing.domain.orders.dto.info.HQOrderItemInfo;
import com.chaing.domain.orders.dto.request.HQOrderItemCreateInfo;
import com.chaing.domain.orders.dto.request.HQOrderUpdateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
import com.chaing.domain.orders.dto.response.HQOrderCancelResponse;
import com.chaing.domain.orders.dto.response.HQOrderCreateResponse;
import com.chaing.domain.orders.dto.response.HQOrderDetailResponse;
import com.chaing.domain.orders.dto.response.HQOrderResponse;
import com.chaing.domain.orders.dto.response.HQOrderStatusUpdateResponse;
import com.chaing.domain.orders.dto.response.HQOrderUpdateResponse;
import com.chaing.domain.orders.dto.response.HQRequestedOrderResponse;
import com.chaing.domain.orders.enums.HQOrderStatus;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.orders.service.HQOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.users.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HQOrderFacade {

    private final HQOrderService hqOrderService;
    private final FranchiseOrderService franchiseOrderService;
    private final ProductService productService;
    private final UserManagementService userManagementService;
    private final FranchiseServiceImpl franchiseService;

    // 발주 조회
    public List<HQOrderResponse> getAllOrders(String username) {
        // hqId username으로 꺼내오는 로직 추가
        Long hqId = 10L;

        // 발주 정보 조회
        // Map<orderId, HQOrderInfo>
        Map<Long, HQOrderInfo> orderInfos = hqOrderService.getAllOrders(hqId, username);

        // 발주 제품 정보 조회
        List<Long> orderIds = orderInfos.keySet().stream().toList();
        // 1. Map<orderId, List<productId>>
        Map<Long, List<Long>> productIdByOrderId = hqOrderService.getAllOrderItemProductId(hqId, orderIds);
        // 2. Map<productId, List<ProductInfo>> productId 별 제품 정보 조회
        List<Long> productIds = productIdByOrderId.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);
        // 3. Map<orderId, quantity> orderId 별 수량 계산
        Map<Long, Integer> quantityByOrderId = productIdByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                ));

        // 반환
        return productIdByOrderId.keySet().stream()
                .<HQOrderResponse>map(orderId -> HQOrderResponse.builder()
                        .orderCode(orderInfos.get(orderId).orderCode())
                        .status(orderInfos.get(orderId).status())
                        .quantity(quantityByOrderId.get(orderId))
                        .username(orderInfos.get(orderId).username())
                        .phoneNumber(orderInfos.get(orderId).phoneNumber())
                        .requestedDate(orderInfos.get(orderId).requestedDate())
                        .manufacturedDate(orderInfos.get(orderId).manufacturedDate())
                        .storedDate(orderInfos.get(orderId).storedDate())
                        .productCode(productInfoByProductId.get(productIdByOrderId.get(orderId).get(0)).productCode())
                        .build())
                .toList();
    }

    // 특정 발주 조회
    public HQOrderDetailResponse getOrderDetail(String username, String orderCode) {
        // hqId username으로 꺼내오는 로직 추가
        Long hqId = 10L;

        // 발주 정보 조회
        HQOrderInfo orderInfo = hqOrderService.getOrder(hqId, orderCode);

        // 발주 제품 정보 조회
        // 1. List<productId> productId 조회
        List<Long> productIds = hqOrderService.getOrderItemProductId(hqId, orderInfo.orderId());
        // 2. Map<productId, List<ProductInfo>> 제품 정보 조회
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);
        // 3. Map<productId, quantity> 제품 별 개수 조회
        Map<Long, Integer> quantityByProductId = productIds.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Math::toIntExact
                        )
                ));
        // 4. List<HQOrderItemInfo>
        List<HQOrderItemInfo> orderItemInfos = productInfoByProductId.keySet().stream()
                .map(productId -> {
                            ProductInfo productInfo = productInfoByProductId.get(productId);
                            Integer quantity = quantityByProductId.get(productId);
                            BigDecimal unitPrice = productInfo.costPrice();
                            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

                            return HQOrderItemInfo.builder()
                                    .productId(productId)
                                    .productCode(productInfo.productCode())
                                    .productName(productInfo.productName())
                                    .quantity(quantity)
                                    .unitPrice(unitPrice)
                                    .totalPrice(totalPrice)
                                    .build();
                        }
                ).toList();

        // 반환
        return HQOrderDetailResponse.builder()
                .orderInfo(orderInfo)
                .items(orderItemInfos)
                .build();
    }

    // 발주 수정
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderUpdateResponse updateOrder(String username, String orderCode, @Valid HQOrderUpdateRequest request) {
        // hqId username으로 꺼내오는 로직 추가
        Long hqId = 10L;

        // orderCode로 발주 조회
        HQOrderInfo orderInfo = hqOrderService.getOrder(hqId, orderCode);

        // 발주 제품의 productIds 조회
        List<Long> productIds = hqOrderService.getOrderItemProductId(hqId, orderInfo.orderId());

        // 제품 정보 조회
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // 발주 제품 데이터 수정
        List<HQOrderItemInfo> itemInfos = hqOrderService.updateOrderItems(hqId, orderCode, request.items(), productInfoByProductId);

        // 발주 정보 수정
        HQOrderInfo updatedOrderInfo = hqOrderService.updateOrder(hqId, orderCode, request.manufactureDate());

        // 반환
        return HQOrderUpdateResponse.builder()
                .orderInfo(updatedOrderInfo)
                .items(itemInfos)
                .build();
    }

    // 발주 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderCancelResponse cancel(String username, String orderCode) {
        // hqId username으로 꺼내오는 로직 추가
        Long hqId = 10L;

        // 취소
        Map<String, HQOrderStatus> cancelOrder = hqOrderService.cancel(hqId, orderCode);

        // 반환
        return HQOrderCancelResponse.builder()
                .orderCode(cancelOrder.keySet().iterator().next())
                .status(cancelOrder.values().iterator().next())
                .build();
    }

    // 가맹점 발주 상태 변경(접수/반려)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<HQOrderStatusUpdateResponse> updateStatus(String username, @Valid HQOrderUpdateStatusRequest request) {
        // hqId username으로 꺼내오는 로직 추가
        Long hqId = 10L;

        // 상태 변경 및 반환
        return franchiseOrderService.updateStatus(request);
    }

    // 발주 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderCreateResponse create(String username, HQOrderCreateRequest request) {
        // hqId username으로 꺼내오는 로직 추가
        Long hqId = 10L;

        // 발주 제품 정보 조회
        List<Long> productIds = request.items().stream()
                .map(HQOrderItemCreateInfo::productId)
                .toList();
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // 발주 생성
        Integer totalQuantity = request.items().stream()
                .map(HQOrderItemCreateInfo::quantity)
                .reduce(0, Integer::sum);
        BigDecimal totalAmount = request.items().stream()
                .map(item -> {
                    ProductInfo productInfo = productInfoByProductId.get(item.productId());

                    if (productInfo == null) {
                        throw new HQOrderException(HQOrderErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return productInfo.costPrice().multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        HQOrderInfo orderInfo = hqOrderService.createOrder(hqId, request, totalQuantity, totalAmount);

        // 발주 제품 생성
        List<HQOrderItemInfo> items = hqOrderService.createOrderItems(orderInfo.orderId(), productInfoByProductId, request.items());

        // 반환
        return HQOrderCreateResponse.builder()
                .orderInfo(orderInfo)
                .items(items)
                .build();
    }

    // 가맹점 발주 요청 조회
    public List<HQRequestedOrderResponse> getRequestedOrders(Long userId) {
        // Map<orderId, FranchiseOrderDetail>
        Map<Long, FranchiseOrderDetailCommand> orders = franchiseOrderService.getAllRequestedOrders();

        // List<orderId>
        List<Long> orderIds = orders.keySet().stream().toList();

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemByOrderId = franchiseOrderService.getAllRequestedOrderItem(orderIds);

        // Map<userId, username>
        Map<Long, String> usernameByUserId = orders.values().stream()
                .collect(Collectors.toMap(
                        FranchiseOrderDetailCommand::userId,
                        order -> userManagementService.getUsernameByUserId(order.userId()),
                        (a, b) -> a
                ));

        // Map<userId, franchiseCode>
        Map<Long, String> franchiseCodeByUserId = orders.values().stream()
                .collect(Collectors.toMap(
                        FranchiseOrderDetailCommand::userId,
                        order -> franchiseService.getById(userManagementService.getFranchiseIdByUserId(order.userId())).code(),
                        (a, b) -> a
                ));

        // Map<orderItemId, FranchiseOrderItemCommand>
        Map<Long, FranchiseOrderItemCommand> orderItemByOrderItemId = orderItemByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        FranchiseOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // Map<orderId, receiver>
        Map<Long, String> receiverByOrderId = orders.values().stream()
                .collect(Collectors.toMap(
                        FranchiseOrderDetailCommand::orderId,
                        command -> userManagementService.getUsernameByUserId(command.userId())
                ));

        // Map<orderId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdsByOrderId = orderItemByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(FranchiseOrderItemCommand::orderItemId)
                                .toList()
                ));

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = orderItemByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        FranchiseOrderItemCommand::orderItemId,
                        FranchiseOrderItemCommand::productId
                ));

        // Map<orderId, Map<productId, List<orderItemId>>>
        Map<Long, Map<Long, List<Long>>> productIdOrderItemIdByOrderId = orderItemByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.groupingBy(
                                        FranchiseOrderItemCommand::productId,
                                        Collectors.mapping(FranchiseOrderItemCommand::orderItemId, Collectors.toList())
                                ))
                ));

        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // 반환
        return productIdOrderItemIdByOrderId.entrySet().stream()
                .flatMap(entry -> {
                    Long orderId = entry.getKey();
                    FranchiseOrderDetailCommand order = orders.get(orderId);
                    Map<Long, List<Long>> orderItemIdsByProductId = entry.getValue();

                    return orderItemIdsByProductId.entrySet().stream()
                            .map(entrySet -> {
                                Long productId = entrySet.getKey();
                                List<Long> orderItemIds = entrySet.getValue();
                                Long orderUserId = order.userId();
                                String username = usernameByUserId.get(orderUserId);
                                String franchiseCode = franchiseCodeByUserId.get(orderUserId);

                                List<FranchiseOrderItemCommand> orderItems = orderItemIds.stream()
                                        .map(orderItemByOrderItemId::get)
                                        .toList();
                                ProductInfo productInfo = productInfoByProductId.get(productId);

                                Integer quantity = orderItems.stream()
                                        .map(FranchiseOrderItemCommand::quantity)
                                        .reduce(0, Integer::sum);

                                return HQRequestedOrderResponse.builder()
                                        .orderCode(order.orderCode())
                                        .franchiseCode(franchiseCode)
                                        .receiver(username)
                                        .productCode(productInfo.productCode())
                                        .status(order.orderStatus())
                                        .quantity(quantity)
                                        .deliveryDate(order.deliveryDate())
                                        .build();
                            });
                })
                .toList();
    }
}
