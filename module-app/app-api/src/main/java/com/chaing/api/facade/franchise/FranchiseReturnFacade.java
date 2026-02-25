package com.chaing.api.facade.franchise;

import com.chaing.core.dto.returns.response.FranchiseOrderInfo;
import com.chaing.core.dto.returns.request.OrderItemIdAndSerialCode;
import com.chaing.core.dto.returns.request.ReturnToInventoryRequest;
import com.chaing.core.dto.returns.request.ReturnToProductRequest;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.returns.dto.command.ReturnItemCreateCommand;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.FranchiseReturnItemCreateRequest;
import com.chaing.domain.returns.dto.request.FranchiseReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemCreateResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnDetailResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnInfo;
import com.chaing.domain.returns.dto.response.FranchiseReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnProductInfo;
import com.chaing.domain.returns.dto.response.FranchiseReturnResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnUpdateResponse;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.dto.response.ReturnItemInfo;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.service.FranchiseReturnService;
import com.chaing.domain.returns.service.FranchiseService;
import com.chaing.domain.returns.service.InventoryService;
import com.chaing.domain.returns.service.ProductService;
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

    // 가짜
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final FranchiseService franchiseService;

    // 반품 전체 조회
    public List<FranchiseReturnResponse> getAllReturns(String username) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 반품 정보 조회
        List<FranchiseReturnAndReturnItemResponse> returnInfos = franchiseReturnService.getAllReturns(franchiseId);

        // 발주 코드 조회
        List<Long> orderIds = returnInfos.stream()
                .map(FranchiseReturnAndReturnItemResponse::franchiseOrderId)
                .toList();
        Map<Long, String> orderIdAndCode = franchiseOrderService.getAllOrderCode(orderIds);

        // 제품 코드 조회
        List<Long> orderItemIds = returnInfos.stream()
                .map(FranchiseReturnAndReturnItemResponse::franchiseOrderItemId)
                .toList();

        // 1. orderItem에서 orderItemId로 serialCode 가져옴
        List<OrderItemIdAndSerialCode> orderItemIdAndSerialCodes = franchiseOrderService.getSerialCodes(orderItemIds);
        System.out.println("facade에서: " + orderItemIdAndSerialCodes.get(0).toString());
        Map<Long, String> orderItemIdAndSerialCodesMap = orderItemIdAndSerialCodes.stream()
                .collect(Collectors.toMap(
                        OrderItemIdAndSerialCode::orderItemId,
                        OrderItemIdAndSerialCode::serialCode
                ));

        // 2. inventory에서 serialCode로 productId, boxCode 가져옴
        List<String> serialCodes = orderItemIdAndSerialCodes.stream()
                .map(OrderItemIdAndSerialCode::serialCode)
                .toList();

        List<ReturnToInventoryRequest> inventoryRequests = inventoryService.getProducts(serialCodes);
        Map<String, ReturnToInventoryRequest> serialCodeAndInventoryMap = inventoryRequests.stream()
                .collect(Collectors.toMap(
                        ReturnToInventoryRequest::serialCode,
                        Function.identity()
                ));

        List<Long> productIds = inventoryRequests.stream()
                .map(ReturnToInventoryRequest::productId)
                .toList();

        // 3. product에서 productId로 productName, productCode 가져옴
        List<ReturnToProductRequest> productRequests = productService.getProducts(productIds);
        Map<Long, ReturnToProductRequest> productIdAndProductMap = productRequests.stream()
                .collect(Collectors.toMap(
                        ReturnToProductRequest::productId,
                        Function.identity()
                ));

        return returnInfos.stream()
                .map(info -> {
                    Long orderId = info.franchiseOrderId();
                    Long orderItemId = info.franchiseOrderItemId();

                    String orderCode = orderIdAndCode.get(orderId);
                    String serialCode = orderItemIdAndSerialCodesMap.get(orderItemId);

                    ReturnToInventoryRequest inventory = serialCodeAndInventoryMap.get(serialCode);

                    Long productId = inventory.productId();
                    String boxCode = inventory.boxCode();

                    ReturnToProductRequest product = productIdAndProductMap.get(productId);

                    String productName = product.productName();
                    String productCode = product.productCode();
                    BigDecimal unitPrice = product.unitPrice();

                    return FranchiseReturnResponse.builder()
                            .returnCode(info.returnCode())
                            .status(info.status())
                            .unitPrice(unitPrice)
                            .franchiseOrderId(info.franchiseOrderId())
                            .type(info.type())
                            .requestedDate(info.requestedDate())
                            .boxCode(boxCode)
                            .serialCode(serialCode)
                            .orderCode(orderCode)
                            .productCode(productCode)
                            .productName(productName)
                            .build();
                })
                .toList();
    }

    // 반품 상세조회
    public FranchiseReturnDetailResponse getReturn(String username, String returnCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 반품 정보
        FranchiseReturnInfo returnInfo = franchiseReturnService.getReturn(username, franchiseId, returnCode);

        // 발주 정보
        String orderCode = franchiseOrderService.getOrderCode(franchiseId, returnInfo.orderId());

        // 반품 제품 정보
        List<Long> orderItemIds = franchiseReturnService.getAllReturnItemOrderItemId(returnCode);

        // SerialCode
        List<String> serialCodes = franchiseOrderService.getSerialCodeList(orderItemIds);

        // 제품 정보
        List<ReturnToInventoryRequest> inventoryRequests = inventoryService.getProducts(serialCodes);
        List<FranchiseReturnProductInfo> productInfos = productService.getProduct(returnCode);

        // 가맹점 코드
        String franchiseCode = franchiseService.getFranchise(franchiseId);

        return FranchiseReturnDetailResponse.builder()
                .returnCode(returnCode)
                .orderCode(orderCode)
                .franchiseCode(franchiseCode)
                .requestedDate(returnInfo.requestedDate())
                .status(returnInfo.status())
                .username(username)
                .phoneNumber(returnInfo.phoneNumber())
                .returnType(returnInfo.type())
                .description(returnInfo.description())
                .items(
                        inventoryRequests.stream()
                                .map(request -> {
                                    String serialCode = request.serialCode();
                                    Long productId = request.productId();
                                    String boxCode = request.boxCode();

                                    FranchiseReturnProductInfo product = productInfos.stream()
                                            .filter(productInfo -> productInfo.serialCode().equals(serialCode))
                                            .findFirst()
                                            .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND));

                                    String productCode = product.productCode();
                                    String productName = product.productName();
                                    BigDecimal unitPrice = product.unitPrice();

                                    return FranchiseReturnItemResponse.builder()
                                            .boxCode(boxCode)
                                            .serialCode(serialCode)
                                            .productCode(productCode)
                                            .productName(productName)
                                            .unitPrice(unitPrice)
                                            .build();
                                })
                                .toList()
                )
                .build();
    }

    // 반품 수정
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseReturnUpdateResponse updateReturn(String username, List<FranchiseReturnUpdateRequest> requests, String returnCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 1. 제품 식별코드, 박스코드로 FranchiseInventory에서 productId 조회
        List<Long> productIds = inventoryService.getProductsBySerialCodeAndBoxCode(requests);
        // 2. productId로 Product에서 productCode, productName, unitPrice 조회
        List<FranchiseReturnProductInfo> productInfos = productService.getProduct(returnCode);
        // 3. 조회한 값들로 ReturnItem 수정
        Map<String, Long> orderItemIds = requests.stream()
                .collect(Collectors.toMap(
                        FranchiseReturnUpdateRequest::serialCode,
                        FranchiseReturnUpdateRequest::orderItemId
                ));
        List<FranchiseReturnProductInfo> updateInfos = franchiseReturnService.updateReturnItems(productInfos, returnCode, orderItemIds);

        // 4. 반품 정보
        ReturnInfo returnInfo = franchiseReturnService.getReturnInfo(username, franchiseId, returnCode);

        // 5. 필요한 값들 반환
        return FranchiseReturnUpdateResponse.builder()
                .returnCode(returnInfo.returnCode())
                .status(returnInfo.status())
                .franchiseOrderId(returnInfo.franchiseOrderId())
                .type(returnInfo.type())
                .requestedDate(returnInfo.requestedDate())
                .items(updateInfos)
                .build();
    }

    // 반품 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String cancel(String username, String returnCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        return franchiseReturnService.cancel(franchiseId, username, returnCode);
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
        List<FranchiseReturnProductInfo> productInfos = productService.getProducts(productCodes);
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
        String franchiseCode = franchiseService.getFranchise(franchiseId);
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
        List<ReturnItemInfo> returnItemInfos = franchiseReturnService.createReturnItems(returnInfo.returnCode(), orderItemIds);

        // 결과 반환
        return new FranchiseReturnAndReturnItemCreateResponse(
                returnInfo,
                returnItemInfos
        );
    }

    // 반품 가능 대상 발주 조회
    public List<FranchiseReturnTargetResponse> getAllTargets(String username) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 발주 조회
        List<FranchiseReturnTargetResponse> infos = franchiseOrderService.getAllTargetOrders(franchiseId);

        // 결과 반환
        return infos;
    }
}