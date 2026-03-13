package com.chaing.api.facade.franchise;

import com.chaing.core.dto.command.FranchiseInventoryCommand;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.request.FranchiseReturnUpdateRequest;
import com.chaing.core.dto.returns.response.FranchiseOrderInfo;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.dto.command.FranchiseReturnItemCreateCommand;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCommand;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.FranchiseReturnDeliveryRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnCreateResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnDeliveryResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnDetailResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnTargetOrderItem;
import com.chaing.domain.returns.dto.response.FranchiseReturnUpdateResponse;
import com.chaing.domain.returns.dto.response.ReturnCreateResponse;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.service.FranchiseReturnService;
import com.chaing.domain.returns.service.ReturnCodeGenerator;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseReturnFacade {

    private final FranchiseReturnService franchiseReturnService;
    private final FranchiseOrderService franchiseOrderService;
    private final UserManagementService userManagementService;
    private final ProductService productService;
    private final ReturnCodeGenerator generator;
    private final InventoryService inventoryService;
    private final FranchiseServiceImpl franchiseService;

    // 반품 전체 조회
    public List<FranchiseReturnResponse> getAllReturns(Long userId) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // Map<returnId, ReturnCommand>
        Map<Long, ReturnCommand> returns = franchiseReturnService.getAllReturns(franchiseId);
        log.info("returns: {}", returns);

        // List<returnId>
        List<Long> returnIds = returns.keySet().stream().toList();

        // Map<returnId, List<ReturnItemCommand>>
        Map<Long, List<ReturnItemCommand>> returnItemByReturnId = franchiseReturnService.getAllReturnItemByReturnIds(returnIds);

        // Map<returnItemId, returnId>
        Map<Long, Long> returnIdByReturnItemId = returnItemByReturnId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        ReturnItemCommand::returnItemId,
                        ReturnItemCommand::returnId
                ));

        // Map<returnItemId, orderItemId>
        Map<Long, Long> orderItemIdByReturnItemId = returnItemByReturnId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        ReturnItemCommand::returnItemId,
                        ReturnItemCommand::orderItemId
                ));

        // List<orderItemId>
        List<Long> orderItemIds = orderItemIdByReturnItemId.values().stream().toList();

        // Map<returnItemId, productId>
        Map<Long, Long> productIdByReturnItemId = franchiseOrderService.getProductIdByReturnItemId(orderItemIdByReturnItemId);

        // 발주 코드 조회
        // List<orderId>
        List<Long> orderIds = returns.values().stream()
                .map(ReturnCommand::orderId)
                .toList();
        // Map<orderId, orderCode>
        Map<Long, String> orderCodeByOrderId = franchiseOrderService.getAllOrderCodeByOrderIds(orderIds);

        // List<productId>
        List<Long> productIds = productIdByReturnItemId.values().stream().distinct().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        return returnItemByReturnId.entrySet().stream()
                .flatMap(entry -> {
                    Long returnId = entry.getKey();
                    List<ReturnItemCommand> items = entry.getValue();
                    ReturnCommand returnCommand = returns.get(returnId);

                    Map<Long, List<ReturnItemCommand>> itemsByProductId = items.stream()
                            .collect(Collectors.groupingBy(
                                    item -> productIdByReturnItemId.get(item.returnItemId())
                            ));

                    return itemsByProductId.entrySet().stream()
                            .map(productEntry -> {
                                Long productId = productEntry.getKey();
                                List<ReturnItemCommand> productItems = productEntry.getValue();
                                ProductInfo productInfo = productInfoByProductId.get(productId);

                                if (productInfo == null) {
                                    throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                                }

                                return FranchiseReturnResponse.builder()
                                        .returnCode(returnCommand.returnCode())
                                        .status(returnCommand.status())
                                        .productCode(productInfo.productCode())
                                        .productName(productInfo.productName())
                                        .unitPrice(productInfo.tradePrice())
                                        .quantity(productItems.size())
                                        .totalPrice(productInfo.tradePrice().multiply(BigDecimal.valueOf(productItems.size())))
                                        .type(returnCommand.type())
                                        .requestedDate(returnCommand.requestedAt())
                                        .build();
                            });
                })
                .toList();
    }

    // 반품 상세조회
    public FranchiseReturnDetailResponse getReturn(Long userId, String returnCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // franchiseCode
        String franchiseCode = franchiseService.getById(franchiseId).businessNumber();

        // UserInfo
        String username = userManagementService.getUsernameByUserId(userId);
        String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);

        // ReturnCommand
        ReturnCommand returnCommand = franchiseReturnService.getReturn(userId, franchiseId, returnCode);

        // FranchiseOrderDetailCommand
        FranchiseOrderDetailCommand orderCommand = franchiseOrderService.getOrderByOrderId(franchiseId, userId, returnCommand.orderId());

        // Map<returnItemId, ReturnItemCommand>
        Map<Long, ReturnItemCommand> returnItemByReturnItemId = franchiseReturnService.getReturnItemsByReturnId(returnCommand.returnId());

        // Map<returnItemId, orderItemId>
        Map<Long, Long> orderItemIdByReturnItemId = returnItemByReturnItemId.values().stream()
                .collect(Collectors.toMap(
                        ReturnItemCommand::returnItemId,
                        ReturnItemCommand::orderItemId
                ));

        // List<returnItemId>
        List<Long> returnItemIds = returnItemByReturnItemId.keySet().stream().toList();

        // List<orderItemId>
        List<Long> orderItemIds = returnItemByReturnItemId.values().stream()
                .map(ReturnItemCommand::orderItemId)
                .toList();

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = franchiseOrderService.getProductIdByOrderItemId(orderItemIds);

        // Map<returnItemId, productId>
        Map<Long, Long> productIdByReturnItemId = franchiseOrderService.getProductIdByReturnItemId(orderItemIdByReturnItemId);

        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // List<FranchiseReturnItemResponse>
        List<FranchiseReturnItemResponse> itemResponses = returnItemByReturnItemId.entrySet().stream()
                .map(entry -> {
                    Long returnItemId = entry.getKey();
                    Long productId = productIdByReturnItemId.get(returnItemId);

                    ReturnItemCommand returnItemcommand = entry.getValue();
                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    if (productInfo == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return FranchiseReturnItemResponse.builder()
                            .boxCode(returnItemcommand.boxCode())
                            .productCode(productInfo.productCode())
                            .productName(productInfo.productName())
                            .unitPrice(productInfo.retailPrice())
                            .build();
                })
                .toList();

        // 반환
        return FranchiseReturnDetailResponse.builder()
                .returnCode(returnCommand.returnCode())
                .orderCode(orderCommand.orderCode())
                .franchiseCode(franchiseCode)
                .requestedDate(returnCommand.requestedAt())
                .status(returnCommand.status())
                .username(username)
                .phoneNumber(phoneNumber)
                .returnType(returnCommand.type())
                .description(returnCommand.description())
                .items(itemResponses)
                .build();
    }

    // 반품 수정
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseReturnUpdateResponse updateReturn(Long userId, List<FranchiseReturnUpdateRequest> requests, String returnCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // ReturnCommand
        ReturnCommand returnCommand = franchiseReturnService.getReturn(userId, franchiseId, returnCode);

        // FranchiseOrderDetailCommand
        FranchiseOrderDetailCommand orderCommand = franchiseOrderService.getOrderByOrderId(franchiseId, userId, returnCommand.orderId());

        // List<boxCode>
        List<String> boxCodes = requests.stream().map(FranchiseReturnUpdateRequest::boxCode).toList();

        // Map<boxCode, FranchiseInventoryCommand>
        Map<String, FranchiseInventoryCommand> inventoryByBoxCode = inventoryService.getInventoriesByBoxCode(boxCodes);
        log.info("inventoryByBoxCode: {}" , inventoryByBoxCode);
        // Map<returnItemId, ReturnItemCommand>
        Map<Long, ReturnItemCommand> returnItemByReturnItemId = franchiseReturnService.getReturnItemsByReturnId(returnCommand.returnId());

        // Map<returnItemId, orderItemId>
        Map<Long, Long> orderItemIdByReturnItemId = returnItemByReturnItemId.values().stream()
                .collect(Collectors.toMap(
                        ReturnItemCommand::returnItemId,
                        ReturnItemCommand::orderItemId
                ));

        // 수정
        List<ReturnItemCommand> updatedReturnItems = franchiseReturnService.updateReturnItems(boxCodes, returnCode, inventoryByBoxCode);

        // List<returnItemId>
        List<Long> returnItemIds = returnItemByReturnItemId.keySet().stream().toList();

        // List<orderItemId>
        List<Long> orderItemIds = inventoryByBoxCode.values().stream()
                .map(FranchiseInventoryCommand::orderItemId)
                .toList();

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = franchiseOrderService.getProductIdByOrderItemId(orderItemIds);

        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // List<FranchiseReturnItemResponse>
        List<FranchiseReturnItemResponse> itemResponses = updatedReturnItems.stream()
                .map(item -> {
                            Long orderItemId = item.orderItemId();
                            Long productId = productIdByOrderItemId.get(orderItemId);

                            ProductInfo productInfo = productInfoByProductId.get(productId);

                            if (productInfo == null) {
                                throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                            }

                            return FranchiseReturnItemResponse.builder()
                                    .boxCode(item.boxCode())
                                    .productCode(productInfo.productCode())
                                    .productName(productInfo.productName())
                                    .unitPrice(productInfo.retailPrice())
                                    .build();
                        }
                )
                .toList();

        // 반환
        return FranchiseReturnUpdateResponse.builder()
                .returnCode(returnCode)
                .orderCode(orderCommand.orderCode())
                .items(itemResponses)
                .build();
    }

    // 반품 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String cancel(Long userId, String returnCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        return franchiseReturnService.cancel(franchiseId, userId, returnCode);
    }

    // 반품 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public ReturnCreateResponse create(Long userId, FranchiseReturnCreateRequest request) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // franchiseCode
        String franchiseCode = franchiseService.getById(franchiseId).code();

        // 경고 횟수
        int warningCount = franchiseService.getWarningCount(franchiseId);

        if (warningCount > 2) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_WARNING_COUNT);
        }

        // 가맹점 경고 횟수 증가
        franchiseService.addWarning(franchiseId);

        // FranchiseOrderDetailCommand
        FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByOrderCode(franchiseId, userId, request.orderCode());

        // Order 상태가 배송완료 아니면 예외 발생
        if (order.orderStatus() != FranchiseOrderStatus.COMPLETED) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_ORDER_STATUS);
        }

        // returnCode
        String returnCode = generator.generate(franchiseCode);

        // ReturnCommand 생성
        ReturnCommand returnCommand = franchiseReturnService.createReturn(franchiseId, order.orderId(), returnCode, userId, request);

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItems = franchiseOrderService.getOrderItemsByOrderId(order.orderId());

        // List<orderItemId>
        List<Long> orderItemIds = orderItems.values().stream()
                .flatMap(List::stream)
                .map(FranchiseOrderItemCommand::orderItemId)
                .toList();

        // Map<orderItemId, FranchiseInventoryCommand>
        Map<Long, FranchiseInventoryCommand> inventoryByOrderItemId = inventoryService.getInventoriesByOrderItemIds(orderItemIds);

        // Map<orderItemId, boxCode>
        Map<Long, String> boxCodeByOrderItemId = inventoryByOrderItemId.values().stream()
                .filter(inventory -> request.boxCodes().contains(inventory.boxCode()))
                .collect(Collectors.toMap(
                        FranchiseInventoryCommand::orderItemId,
                        FranchiseInventoryCommand::boxCode
                ));

        // List<ReturnItemCommand> 생성
        List<ReturnItemCommand> createdItems = franchiseReturnService.createReturnItems(returnCommand.returnId(), boxCodeByOrderItemId);

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = franchiseOrderService.getProductIdByOrderItemId(orderItemIds);

        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // List<FranchiseReturnItemCreateCommand>
        List<FranchiseReturnItemCreateCommand> itemResponses = createdItems.stream()
                .map(item -> {
                    Long orderItemId = item.orderItemId();
                    Long productId = productIdByOrderItemId.get(orderItemId);
                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    return FranchiseReturnItemCreateCommand.builder()
                            .boxCode(item.boxCode())
                            .productCode(productInfo.productCode())
                            .build();
                })
                .toList();

        // 반환
        return ReturnCreateResponse.builder()
                .returnCode(returnCommand.returnCode())
                .items(itemResponses)
                .build();
    }

    // 반품 가능 대상 발주 조회
    public List<FranchiseReturnTargetResponse> getAllTargets(Long userId) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // username
        String username = userManagementService.getUsernameByUserId(userId);

        // 발주 조회 및 반환
        return franchiseOrderService.getAllTargetOrders(franchiseId, userId, username);
    }

    // 발주 정보 조회
    public FranchiseReturnCreateResponse getReturnCreateInfo(Long userId, String orderCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // username
        String username = userManagementService.getUsernameByUserId(userId);

        // phoneNumber
        String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);

        // franchiseCode
        String franchiseCode = franchiseService.getById(franchiseId).businessNumber();

        // FranchiseOrderDetailCommand
        FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByOrderCode(franchiseId, userId, orderCode);
        log.info("order: {}", order);

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemsByOrderId = franchiseOrderService.getOrderItemsByOrderId(order.orderId());
        log.info("orderItemsByOrderId: {}", orderItemsByOrderId);

        // List<FranchiseOrderItemCommand>
        List<FranchiseOrderItemCommand> orderItems = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .toList();

        // List<orderItemId>
        List<Long> orderItemIds = orderItems.stream()
                .map(FranchiseOrderItemCommand::orderItemId)
                .toList();
        log.info("orderItemIds={}", orderItemIds);

        // Map<orderItemId, FranchiseInventoryCommand>
        Map<Long, FranchiseInventoryCommand> inventoryByOrderItemId = inventoryService.getInventoriesByOrderItemIds(orderItemIds);

        // List<productId>
        List<Long> productIds = orderItems.stream()
                .map(FranchiseOrderItemCommand::productId)
                .toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // FranchiseOrderInfo
        FranchiseOrderInfo orderInfo = FranchiseOrderInfo.builder()
                .orderCode(order.orderCode())
                .username(username)
                .phoneNumber(phoneNumber)
                .franchiseCode(franchiseCode)
                .build();

        // List<FranchiseReturnTargetOrderItem>
        List<FranchiseReturnTargetOrderItem> items = orderItems.stream()
                .map(item -> {
                    Long orderItemId = item.orderItemId();
                    log.info("orderItemId: {}", orderItemId);
                    FranchiseInventoryCommand inventory = inventoryByOrderItemId.get(orderItemId);
                    log.info("inventoryByOrderItemId: {}", inventoryByOrderItemId);
                    log.info("inventory: {}", inventory);
                    Long productId = inventory.productId();
                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    if (productInfo == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return FranchiseReturnTargetOrderItem.builder()
                            .boxCode(inventory.boxCode())
                            .productCode(productInfo.productCode())
                            .productName(productInfo.productName())
                            .unitPrice(productInfo.retailPrice())
                            .build();
                })
                .toList();

        return FranchiseReturnCreateResponse.builder()
                .orderInfo(orderInfo)
                .items(items)
                .build();
    }

    // 외부 모듈용 반품 제품 출고
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<FranchiseReturnDeliveryResponse> delivery(List<FranchiseReturnDeliveryRequest> requests) {
        // Set<boxCode>
        Set<String> requestedBoxCodes = requests.stream()
                .map(FranchiseReturnDeliveryRequest::boxCode)
                .collect(Collectors.toSet());

        // 출고 처리
        // Map<returnCode, List<boxCode>>
        Map<String, List<String>> boxCodesByReturnCode = franchiseReturnService.delivery(requestedBoxCodes);

        // 반환
        return boxCodesByReturnCode.entrySet().stream()
                .map(entry -> FranchiseReturnDeliveryResponse.of(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }
}