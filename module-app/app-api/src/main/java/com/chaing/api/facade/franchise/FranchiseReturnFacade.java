package com.chaing.api.facade.franchise;

import com.chaing.core.dto.command.FranchiseInventoryCommand;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.returns.request.ReturnToInventoryRequest;
import com.chaing.core.dto.returns.request.ReturnToProductRequest;
import com.chaing.core.dto.returns.response.FranchiseOrderInfo;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.command.ReturnItemBoxCodeCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCommand;
import com.chaing.domain.returns.dto.command.ReturnItemCreateCommand;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.FranchiseReturnItemCreateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemCreateResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnCreateResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnDetailResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnProductInfo;
import com.chaing.domain.returns.dto.response.FranchiseReturnResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnTargetOrderItem;
import com.chaing.domain.returns.dto.response.FranchiseReturnUpdateResponse;
import com.chaing.domain.returns.dto.response.ReturnAndOrderInfo;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.service.FranchiseReturnService;
import com.chaing.domain.users.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseReturnFacade {

    private final FranchiseReturnService franchiseReturnService;
    private final FranchiseOrderService franchiseOrderService;
    private final UserManagementService userManagementService;
    private final ProductService productService;

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

        // Map<orderItemId, FranchiseOrderItemCommand>
        Map<Long, FranchiseOrderItemCommand> orderItemByOrderItemId = franchiseOrderService.getOrderItemsByOrderItemIds(orderItemIds);

        // Map<returnItemId, productId>
        Map<Long, Long> productIdByReturnItemId = franchiseOrderService.getProductIdByReturnItemId(orderItemIdByReturnItemId);

        // List<returnItemId>
        List<Long> returnItemIds = productIdByReturnItemId.keySet().stream().toList();

        // Map<returnItemId, List<ReturnItemBoxCodeCommand>>
        Map<Long, List<ReturnItemBoxCodeCommand>> returnItemBoxCodeByReturnItemId = franchiseReturnService.getReturnItemBoxCodeByReturnItemId(returnItemIds);

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

        return productIdByReturnItemId.entrySet().stream()
                .map(entry -> {
                    Long returnItemId = entry.getKey();
                    Long orderItemId = orderItemIdByReturnItemId.get(returnItemId);
                    Long productId = entry.getValue();
                    Long returnId = returnIdByReturnItemId.get(returnItemId);

                    ReturnCommand returnCommand = returns.get(returnId);
                    ProductInfo productInfo = productInfoByProductId.get(productId);
                    FranchiseOrderItemCommand orderItemCommand = orderItemByOrderItemId.get(orderItemId);
                    List<ReturnItemBoxCodeCommand> boxCodeCommands = returnItemBoxCodeByReturnItemId.get(returnItemId);

                    Long orderId = returnCommand.orderId();
                    String orderCode = orderCodeByOrderId.get(orderId);
                    int quantity = boxCodeCommands.size();

                    if (productInfo == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return FranchiseReturnResponse.builder()
                            .returnCode(returnCommand.returnCode())
                            .status(returnCommand.status())
                            .orderCode(orderCode)
                            .productCode(productInfo.productCode())
                            .productName(productInfo.productName())
                            .unitPrice(productInfo.retailPrice())
                            .quantity(quantity)
                            .totalPrice(orderItemCommand.unitPrice().multiply(BigDecimal.valueOf(quantity)))
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

        // Map<returnItemId, List<ReturnItemBoxCodeCommand>>
        Map<Long, List<ReturnItemBoxCodeCommand>> returnItemBoxCodesByReturnItemId = franchiseReturnService.getReturnItemBoxCodeByReturnItemId(returnItemIds);

        // List<ReturnItemBoxCodeCommand>
        List<ReturnItemBoxCodeCommand> returnItemBoxCodeCommands = returnItemBoxCodesByReturnItemId.values().stream()
                .flatMap(List::stream)
                .toList();

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
        List<FranchiseReturnItemResponse> itemResponses = returnItemBoxCodeCommands.stream()
                .map(boxCodeCommand -> {
                    Long returnItemId = boxCodeCommand.returnItemId();
                    Long productId = productIdByReturnItemId.get(returnItemId);

                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    return FranchiseReturnItemResponse.builder()
                            .boxCode(boxCodeCommand.boxCode())
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

        // 수정 Map<returnItemId, orderItemId>, boxCodes, Map<boxCode, orderItemId>
        List<ReturnItemCommand> updatedReturnItems = franchiseReturnService.updateReturnItems(boxCodes, orderItemIdByReturnItemId, returnCode, orderItemIdByBoxCode);

        // List<returnItemId>
        List<Long> returnItemIds = returnItemByReturnItemId.keySet().stream().toList();

        // Map<returnItemId, List<ReturnItemBoxCodeCommand>>
        Map<Long, List<ReturnItemBoxCodeCommand>> returnItemBoxCodeByReturnItemId = franchiseReturnService.getReturnItemBoxCodeByReturnItemId(returnItemIds);

        // List<ReturnItemBoxCodeCommand>
        List<ReturnItemBoxCodeCommand> returnItemBoxCodeCommands = returnItemBoxCodeByReturnItemId.values().stream()
                .flatMap(List::stream)
                .toList();

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
        List<FranchiseReturnItemResponse> itemResponses = returnItemBoxCodeCommands.stream()
                .map(boxCode -> {
                    Long returnItemId = boxCode.returnItemId();
                    Long orderItemId = orderItemIdByReturnItemId.get(returnItemId);
                    Long productId = productIdByOrderItemId.get(orderItemId);

                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    if (productInfo == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return FranchiseReturnItemResponse.builder()
                            .boxCode(boxCode.boxCode())
                            .productCode(productInfo.productCode())
                            .productName(productInfo.productName())
                            .unitPrice(productInfo.retailPrice())
                            .build();
                })
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
    public FranchiseReturnAndReturnItemCreateResponse create(String username, FranchiseReturnCreateRequest request) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // request의 product에 대한 내용 검증
        // 1. productInfo 중 productCode에 해당하는 데이터 조회
        Set<String> productCodes = request.items().stream()
                .map(FranchiseReturnItemCreateRequest::productCode)
                .collect(Collectors.toSet());
        // 2. 실제 제품 정보
        List<FranchiseReturnProductInfo> productInfos = fakeReturnProductService.getProducts(productCodes);
        // 3. 실제 제품 정보와 productCode Map으로
        Map<String, FranchiseReturnProductInfo> productInfoByProductCode = productInfos.stream()
                        .collect(Collectors.toMap(
                                FranchiseReturnProductInfo::productCode,
                                Function.identity()
                        ));
        // 4. 검증
        request.items().forEach(item -> {
                    FranchiseReturnProductInfo productInfo = productInfoByProductCode.get(item.productCode());
                    if (productInfo == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_PRODUCT_INFO);
                    }

                    if (!productInfo.productName().equals(item.productName()) || !productInfo.unitPrice().equals(item.unitPrice())) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_PRODUCT_INFO);
                    }
                });

        // 반품 생성
        // 1.1. 발주 정보 조회
        String franchiseCode = fakeReturnFranchiseService.getFranchise(franchiseId);
        FranchiseOrderInfo orderInfo = franchiseOrderService.getOrderInfo(franchiseId, username, request.orderCode(), franchiseCode);
        // 1.2. 반품 생성
        ReturnInfo returnInfo = franchiseReturnService.createReturn(franchiseId, request, orderInfo);

        // 반품 제품 생성
        // 1. franchiseInventory에서 boxCode에 해당되는 serialCode 조회
        // 1.1. Map<boxCode, List<serialCode>>
        Map<String, List<String>> serialCodeListByBoxCode = request.items().stream()
                .map(FranchiseReturnItemCreateRequest::boxCode)
                .collect(Collectors.toMap(
                        Function.identity(),
                        boxCode -> {
                            List<String> serialCodes = inventoryService.getSerialCodes(franchiseId, boxCode);

                            if (serialCodes == null || serialCodes.isEmpty()) {
                                throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_BOX_CODE);
                            }

                            return serialCodes;
                        }
                ));
        // 2. franchiseOrderItem에서 serialCode에 해당되는 orderItemId 조회
        // Map<serialCode, orderItemId>
        Map<String, Long> orderItemIdBySerialCode = serialCodeListByBoxCode.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        Function.identity(),
                        serialCode -> {
                            Long orderItemId = franchiseOrderService.getOrderItemId(serialCode);

                            if (orderItemId == null) {
                                throw new FranchiseReturnException(FranchiseReturnErrorCode.ORDER_ITEM_NOT_FOUND);
                            }

                            return orderItemId;
                        }
                ));
        // 3. 필요 값 전달해 반품 제품 생성
        List<ReturnItemCreateCommand> orderItemIds = orderItemIdBySerialCode.entrySet().stream()
                .map(entry -> new ReturnItemCreateCommand(entry.getKey(), entry.getValue()))
                .toList();
        List<ReturnAndOrderInfo> returnAndOrderInfos = franchiseReturnService.createReturnItems(returnInfo.returnCode(), orderItemIds);

        // 결과 반환
        return new FranchiseReturnAndReturnItemCreateResponse(
                returnInfo,
                returnAndOrderInfos
        );
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
    public FranchiseReturnCreateResponse getReturnCreateInfo(String username, String requestedUsername, String orderCode) {
        if (!username.equals(requestedUsername)) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.USER_FORBIDDEN);
        }

        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 발주 정보 조회
        String franchiseCode = fakeReturnFranchiseService.getFranchise(franchiseId);
        FranchiseOrderInfo orderInfo = franchiseOrderService.getOrderInfo(franchiseId, username, orderCode, franchiseCode);

        // 발주 제품 정보 조회
        // 1. orderCode로 orderItem serialCode 조회
        List<String> serialCodes = franchiseOrderService.getSerialCodesByOrderCode(franchiseId, orderCode);
        // 2. orderItem serialCode로 franchiseInventory boxCode, productId 조회
        List<ReturnToInventoryRequest> inventoryInfo = inventoryService.getProducts(serialCodes);
        // 3. productId로 product 조회
        List<Long> productIds = inventoryInfo.stream()
                .map(ReturnToInventoryRequest::productId)
                .toList();
        List<ReturnToProductRequest> productInfos = fakeReturnProductService.getProducts(productIds);
        // 3.1. Map<productId, productCode>
        Map<Long, String> productCodeByproductId = productInfos.stream()
                .collect(Collectors.toMap(
                        ReturnToProductRequest::productId,
                        ReturnToProductRequest::productCode
                ));
        // 3.2. Map<productId, productName>
        Map<Long, String> productNameByproductId = productInfos.stream()
                .collect(Collectors.toMap(
                        ReturnToProductRequest::productId,
                        ReturnToProductRequest::productName
                ));
        // 3.3. Map<productId, unitPrice>
        Map<Long, BigDecimal> unitPriceByproductId = productInfos.stream()
                .collect(Collectors.toMap(
                        ReturnToProductRequest::productId,
                        ReturnToProductRequest::unitPrice
                ));

        // 결과 반환
        return new FranchiseReturnCreateResponse(
                orderInfo,
                inventoryInfo.stream()
                        .map(info -> {
                            return new FranchiseReturnTargetOrderItem(
                                    info.boxCode(),
                                    productCodeByproductId.get(info.productId()),
                                    productNameByproductId.get(info.productId()),
                                    unitPriceByproductId.get(info.productId())
                            );
                        })
                        .toList()
        );
    }
}