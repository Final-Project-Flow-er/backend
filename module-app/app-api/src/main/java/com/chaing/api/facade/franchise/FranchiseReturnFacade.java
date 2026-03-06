package com.chaing.api.facade.franchise;

import com.chaing.core.dto.command.FranchiseInventoryCommand;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.returns.response.FranchiseOrderInfo;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.dto.command.FranchiseReturnItemCreateCommand;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCommand;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnCreateResponse;
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

        // Map<returnId, List<ReturnItemCommand>>
        Map<Long, List<ReturnItemCommand>> returnItemByReturnId = franchiseReturnService.getAllReturnItem();

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

        // List<returnItemId>
        List<Long> returnItemIds = productIdByReturnItemId.keySet().stream().toList();

        // 발주 코드 조회
        // List<orderId>
        List<Long> orderIds = returns.values().stream()
                .map(ReturnCommand::orderId)
                .toList();
        // Map<orderId, orderCode>
        Map<Long, String> orderCodeByOrderId = franchiseOrderService.getAllOrderCodeByOrderIds(orderIds);

        // Map<productId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdsByProductId = franchiseOrderService.getOrderItemIdsAndProductIdsByOrderIds(orderIds);

        // List<productId>
        List<Long> productIds = orderItemIdsByProductId.keySet().stream().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        return orderItemIdsByProductId.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    List<Long> targetOrderItemIds = entry.getValue();
                    Long returnId = returnIdByReturnItemId.get(targetOrderItemIds.get(0));

                    ReturnCommand returnCommand = returns.get(returnId);
                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    Long orderId = returnCommand.orderId();
                    String orderCode = orderCodeByOrderId.get(orderId);

                    int quantity = targetOrderItemIds.size();
                    BigDecimal unitPrice = productInfo.retailPrice();

                    return FranchiseReturnResponse.builder()
                            .returnCode(returnCommand.returnCode())
                            .status(returnCommand.status())
                            .orderCode(orderCode)
                            .productCode(productInfo.productCode())
                            .productName(productInfo.productName())
                            .unitPrice(unitPrice)
                            .quantity(quantity)
                            .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                            .type(returnCommand.type())
                            .requestedDate(returnCommand.requestedAt())
                            .build();
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
    public FranchiseReturnUpdateResponse updateReturn(Long userId, List<String> boxCodes, String returnCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // ReturnCommand
        ReturnCommand returnCommand = franchiseReturnService.getReturn(userId, franchiseId, returnCode);

        // FranchiseOrderDetailCommand
        FranchiseOrderDetailCommand orderCommand = franchiseOrderService.getOrderByOrderId(franchiseId, userId, returnCommand.orderId());

        // Map<boxCode, FranchiseInventoryCommand>
        Map<String, FranchiseInventoryCommand> inventoryByBoxCode = inventoryService.getInventoriesByBoxCode(boxCodes);

        // Map<returnItemId, ReturnItemCommand>
        Map<Long, ReturnItemCommand> returnItemByReturnItemId = franchiseReturnService.getReturnItemsByReturnId(returnCommand.returnId());

        // Map<boxCode, orderItemId>
        Map<String, Long> orderItemIdByBoxCode = returnItemByReturnItemId.values().stream()
                .collect(Collectors.toMap(
                        ReturnItemCommand::boxCode,
                        ReturnItemCommand::orderItemId
                ));

        // Map<returnItemId, orderItemId>
        Map<Long, Long> orderItemIdByReturnItemId = returnItemByReturnItemId.values().stream()
                .collect(Collectors.toMap(
                        ReturnItemCommand::returnItemId,
                        ReturnItemCommand::orderItemId
                ));

        // 수정 반영. input: Map<returnItemId, orderItemId>, boxCodes, Map<boxCode, orderItemId>
        List<ReturnItemCommand> updatedReturnItems = franchiseReturnService.updateReturnItems(boxCodes, orderItemIdByReturnItemId, returnCode, orderItemIdByBoxCode);

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
        String franchiseCode = franchiseService.getById(franchiseId).businessNumber();

        // FranchiseOrderDetailCommand
        FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByOrderCode(franchiseId, userId, request.orderCode());

        // returnCode
        String returnCode = generator.generate(franchiseCode);

        // ReturnCommand 생성
        ReturnCommand returnCommand = franchiseReturnService.createReturn(franchiseId, order.orderId(), returnCode, userId, request);

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItems = franchiseOrderService.getOrderItemsByOrderId(order.orderId());

        // List<orderItemId>
        List<Long> orderItemIds = orderItems.keySet().stream().toList();

        // Map<orderItemId, FranchiseInventoryCommand>
        Map<Long, FranchiseInventoryCommand> inventoryByOrderItemId = inventoryService.getInventoriesByOrderItemIds(orderItemIds);

        // Map<orderItemId, boxCode>
        Map<Long, String> boxCodeByOrderItemId = inventoryByOrderItemId.values().stream()
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

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemsByOrderId = franchiseOrderService.getOrderItemsByOrderId(order.orderId());

        // List<FranchiseOrderItemCommand>
        List<FranchiseOrderItemCommand> orderItems = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .toList();

        // List<orderItemId>
        List<Long> orderItemIds = orderItems.stream()
                .map(FranchiseOrderItemCommand::orderItemId)
                .toList();

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
                    FranchiseInventoryCommand inventory = inventoryByOrderItemId.get(orderItemId);
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
}