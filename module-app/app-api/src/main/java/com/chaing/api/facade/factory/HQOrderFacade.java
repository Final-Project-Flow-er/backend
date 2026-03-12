package com.chaing.api.facade.factory;

import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.businessunits.service.impl.HeadquarterServiceImpl;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.HQOrderCancelCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.FranchiseOrderStatusUpdateRequest;
import com.chaing.domain.orders.dto.request.HQFranchiseOrderCancelRequest;
import com.chaing.domain.orders.dto.request.HQOrderCreateRequest;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.info.HQOrderItemCommand;
import com.chaing.domain.orders.dto.request.HQOrderItemCreateCommand;
import com.chaing.domain.orders.dto.request.HQOrderUpdateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
import com.chaing.domain.orders.dto.response.FranchiseOrderStatusShippingPendingResponse;
import com.chaing.domain.orders.dto.response.HQFranchiseOrderCancelResponse;
import com.chaing.domain.orders.dto.response.HQOrderCancelResponse;
import com.chaing.domain.orders.dto.response.HQOrderCreateResponse;
import com.chaing.domain.orders.dto.response.HQOrderDetailResponse;
import com.chaing.domain.orders.dto.response.HQOrderResponse;
import com.chaing.domain.orders.dto.response.HQOrderStatusUpdateResponse;
import com.chaing.domain.orders.dto.response.HQOrderUpdateResponse;
import com.chaing.domain.orders.dto.response.HQRequestedOrderResponse;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.orders.service.HQOrderService;
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
public class HQOrderFacade {

    private final HQOrderService hqOrderService;
    private final FranchiseOrderService franchiseOrderService;
    private final ProductService productService;
    private final UserManagementService userManagementService;
    private final FranchiseServiceImpl franchiseService;
    private final HeadquarterServiceImpl headquarterService;

    // 발주 조회
    public List<HQOrderResponse> getAllOrders() {
        // 발주 정보 조회
        // Map<orderId, HQOrderCommand>
        Map<Long, HQOrderCommand> orders = hqOrderService.getAllOrders();

        // Map<userId, username>
        Map<Long, String> usernameByUserId = orders.values().stream()
                .collect(Collectors.toMap(
                        HQOrderCommand::userId,
                        command -> userManagementService.getUsernameByUserId(command.userId()),
                        (a , b) -> a
                ));

        // Map<userId, phoneNumber>
        Map<Long, String> phoneNumberByUserId = orders.values().stream()
                .collect(Collectors.toMap(
                        HQOrderCommand::userId,
                        command -> userManagementService.getPhoneNumberByUserId(command.userId()),
                        (a , b) -> a
                ));

        // 발주 제품 정보 조회
        List<Long> orderIds = orders.keySet().stream().toList();

        // Map<orderId, List<HQOrderItemCommand>>
        Map<Long, List<com.chaing.domain.orders.dto.command.HQOrderItemCommand>> orderItemsByOrderId = hqOrderService.getOrderItemIdsByOrderId(orderIds);

        // Map<orderItemId, HQOrderItemCommand>
        Map<Long, com.chaing.domain.orders.dto.command.HQOrderItemCommand> orderItemByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        com.chaing.domain.orders.dto.command.HQOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        com.chaing.domain.orders.dto.command.HQOrderItemCommand::orderItemId,
                        com.chaing.domain.orders.dto.command.HQOrderItemCommand::productId
                ));

        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream()
                .distinct()
                .toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // Map<orderId, Map<productId, List<orderItemId>>>
        Map<Long, Map<Long, List<Long>>> productIdOrderItemIdByOrderId = orderItemsByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.groupingBy(
                                        com.chaing.domain.orders.dto.command.HQOrderItemCommand::productId,
                                        Collectors.mapping(com.chaing.domain.orders.dto.command.HQOrderItemCommand::orderItemId, Collectors.toList())
                                ))
                ));

        // 반환
        return productIdOrderItemIdByOrderId.entrySet().stream()
                .flatMap(entry -> {
                    Long orderId = entry.getKey();
                    HQOrderCommand order = orders.get(orderId);
                    Map<Long, List<Long>> orderItemByProductId = entry.getValue();

                    return orderItemByProductId.entrySet().stream()
                            .map(entrySet -> {
                                Long productId = entrySet.getKey();
                                List<Long> orderItemIds = entrySet.getValue();
                                Long userId = order.userId();
                                String username = usernameByUserId.get(userId);
                                String phoneNumber = phoneNumberByUserId.get(userId);
                                List<com.chaing.domain.orders.dto.command.HQOrderItemCommand> orderItems = orderItemIds.stream()
                                        .map(orderItemByOrderItemId::get)
                                        .toList();
                                Integer quantity = orderItems.stream()
                                        .map(com.chaing.domain.orders.dto.command.HQOrderItemCommand::quantity)
                                        .reduce(0, Integer::sum);
                                ProductInfo productInfo = productInfoByProductId.get(productId);

                                if (productInfo == null) {
                                    throw new HQOrderException(HQOrderErrorCode.PRODUCT_NOT_FOUND);
                                }

                                return HQOrderResponse.builder()
                                        .orderCode(order.orderCode())
                                        .status(order.status())
                                        .quantity(quantity)
                                        .username(username)
                                        .phoneNumber(phoneNumber)
                                        .requestedDate(order.requestedDate())
                                        .manufacturedDate(order.manufacturedDate())
                                        .storedDate(order.storedDate())
                                        .productCode(productInfo.productCode())
                                        .build();
                            });
                })
                .toList();
    }

    // 특정 발주 조회
    public HQOrderDetailResponse getOrderDetail(String orderCode) {
        // 발주 정보 조회
        HQOrderCommand order = hqOrderService.getOrder(orderCode);

        // UserInfo
        Long userId = order.userId();
        String username = userManagementService.getUsernameByUserId(userId);
        String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);

        // Map<orderId, List<HQOrderItemCommand>>
        Map<Long, List<HQOrderItemCommand>> orderItemsByOrderId = hqOrderService.getOrderItemsByOrderId(order.orderId());

        // Map<orderItemId, HQOrderItemCommand>
        Map<Long, HQOrderItemCommand> orderItemByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        HQOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // List<productId>
        List<Long> productIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(HQOrderItemCommand::productId)
                .toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // Map<productId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdByProductId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        HQOrderItemCommand::productId,
                        Collectors.mapping(HQOrderItemCommand::orderItemId, Collectors.toList())
                ));

        // List<HQOrderItemCommand> — productCode 보강
        List<HQOrderItemCommand> items = orderItemByOrderItemId.values().stream()
                .map(item -> {
                    ProductInfo info = productInfoByProductId.get(item.productId());
                    return HQOrderItemCommand.builder()
                            .orderId(item.orderId())
                            .orderItemId(item.orderItemId())
                            .productId(item.productId())
                            .productCode(info != null ? info.productCode() : null)
                            .quantity(item.quantity())
                            .unitPrice(item.unitPrice())
                            .totalPrice(item.totalPrice())
                            .build();
                })
                .toList();

        // 반환
        return HQOrderDetailResponse.builder()
                .orderCode(order.orderCode())
                .status(order.status())
                .username(username)
                .phoneNumber(phoneNumber)
                .requestedDate(order.requestedDate())
                .manufacturedDate(order.manufacturedDate())
                .storedDate(order.storedDate())
                .description(order.description())
                .isRegular(order.isRegular())
                .items(items)
                .build();
    }

    // 발주 수정
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderUpdateResponse updateOrder(Long userId, String orderCode, HQOrderUpdateRequest request) {
        // UserInfo
        String username = userManagementService.getUsernameByUserId(userId);
        String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);

        // HQOrderCommand
        HQOrderCommand order = hqOrderService.getOrderByUserIdAndOrderCodeAndPending(userId, orderCode);

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getAllProductInfo();

        // Map<productCode, ProductInfo>
        Map<String, ProductInfo> productInfoByProductCode = productInfoByProductId.values().stream()
                .collect(Collectors.toMap(
                        ProductInfo::productCode,
                        Function.identity()
                ));
        log.info("productInfoByProductCode: {}", productInfoByProductCode);
        // 발주 제품 데이터 수정
        List<HQOrderItemCommand> items = hqOrderService.updateOrderItems(userId, orderCode, request, productInfoByProductCode);

        // 반환
        return HQOrderUpdateResponse.builder()
                .orderCode(order.orderCode())
                .status(order.status())
                .username(username)
                .phoneNumber(phoneNumber)
                .requestedDate(order.requestedDate())
                .manufacturedDate(order.manufacturedDate())
                .storedDate(order.storedDate())
                .description(order.description())
                .isRegular(order.isRegular())
                .items(items)
                .build();
    }

    // 발주 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderCancelResponse cancel(Long userId, String orderCode) {
        // 취소
        HQOrderCancelCommand cancelOrder = hqOrderService.cancel(userId, orderCode);

        // 반환
        return HQOrderCancelResponse.from(cancelOrder);
    }

    // 가맹점 발주 상태 변경(접수/반려)
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<HQOrderStatusUpdateResponse> updateStatus(HQOrderUpdateStatusRequest request) {
        // 상태 변경 및 반환
        return franchiseOrderService.updateStatus(request);
    }

    // 발주 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderCreateResponse create(Long userId, HQOrderCreateRequest request) {
        // HQCode
        Long hqId = userManagementService.getBusinessUnitIdByUserId(userId);
        String hqCode = headquarterService.getHqCode(hqId);

        // UserInfo
        String username = userManagementService.getUsernameByUserId(userId);
        String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);

        if (!username.equals(request.username()) || !phoneNumber.equals(request.phoneNumber())) {
            throw new HQOrderException(HQOrderErrorCode.INVALID_USER_INFO);
        }

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getAllProductInfo();

        // 발주 생성
        HQOrderCommand order = hqOrderService.createOrder(userId, request, hqCode, productInfoByProductId);

        // 발주 제품 생성 - 수정요망
        List<HQOrderItemCommand> items = hqOrderService.createOrderItems(order.orderId(), productInfoByProductId, request.items());

        // 반환
        return HQOrderCreateResponse.builder()
                .orderCode(order.orderCode())
                .status(order.status())
                .username(username)
                .phoneNumber(phoneNumber)
                .requestedDate(order.requestedDate())
                .storedDate(order.storedDate())
                .description(order.description())
                .isRegular(order.isRegular())
                .items(items)
                .build();
    }

    // 가맹점 발주 요청 조회
    public List<HQRequestedOrderResponse> getRequestedOrders(boolean isPending) {
        // Map<orderId, FranchiseOrderDetail>
        Map<Long, FranchiseOrderDetailCommand> orders;
        if (isPending) {
            // 대기 상태 요청 조회
            orders = franchiseOrderService.getAllRequestedOrders();
        } else {
            // 전체 요청 조회
            orders = franchiseOrderService.getAllOrders();
        }

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
                        order -> {
                            try {
                                return franchiseService.getById(userManagementService.getFranchiseIdByUserId(order.userId())).code();
                            } catch (Exception e) {
                                throw new HQOrderException(HQOrderErrorCode.INVALID_USER_INFO);
                            }
                        },
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

    // 발주 상태 SHIPPING_PENDING으로 수정
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<FranchiseOrderStatusShippingPendingResponse> updateShippingPending(List<FranchiseOrderStatusUpdateRequest> requests) {
        // List<orderCode>
        Set<String> orderCodes = requests.stream().map(FranchiseOrderStatusUpdateRequest::orderCode).collect(Collectors.toSet());

        // 수정
        // Map<orderId, FranchiseOrderCommand>
        Map<Long, FranchiseOrderCommand> orders = franchiseOrderService.updateShippingPending(orderCodes);

        // 반환
        return orders.values().stream()
                .map(FranchiseOrderStatusShippingPendingResponse::from)
                .toList();
    }

    // 가맹점의 발주 요청 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<HQFranchiseOrderCancelResponse> cancelFranchiseOrder(@Valid List<HQFranchiseOrderCancelRequest> request) {
        // 수정
        // Map<orderCode, FranchiseOrderStatus>
        Map<String, FranchiseOrderStatus> statusByOrderCode = franchiseOrderService.cancelFranchiseOrder(request);

        // 반환
        return statusByOrderCode.entrySet().stream()
                .map(HQFranchiseOrderCancelResponse::of)
                .toList();
    }
}
