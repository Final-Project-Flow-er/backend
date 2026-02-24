package com.chaing.api.facade.franchise;

import com.chaing.core.dto.returns.request.OrderItemIdAndSerialCode;
import com.chaing.core.dto.returns.request.ReturnToInventoryRequest;
import com.chaing.core.dto.returns.request.ReturnToProductRequest;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnResponse;
import com.chaing.domain.returns.service.FranchiseReturnService;
import com.chaing.domain.returns.service.InventoryService;
import com.chaing.domain.returns.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FranchiseReturnFacade {

    private final FranchiseReturnService franchiseReturnService;
    private final FranchiseOrderService franchiseOrderService;

    // 가짜
    private final InventoryService inventoryService;
    private final ProductService productService;

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
                    System.out.println("orderItemIdAndSerialCodesMap: " + orderItemIdAndSerialCodesMap.get(orderItemId));
                    String serialCode = orderItemIdAndSerialCodesMap.get(orderItemId);
                    System.out.println("serialCode: " + serialCode);

                    System.out.println(serialCodeAndInventoryMap);
                    ReturnToInventoryRequest inventory = serialCodeAndInventoryMap.get(serialCode);
                    if (inventory == null) {
                        throw new IllegalStateException("Inventory not found for serialCode=" + serialCode
                                + ", orderItemId=" + orderItemId);
                    }
                    Long productId = inventory.productId();
                    String boxCode = inventory.boxCode();

                    ReturnToProductRequest product = productIdAndProductMap.get(productId);
                    if (product == null) {
                        throw new IllegalStateException("Product not found for productId=" + productId
                                + ", serialCode=" + serialCode);
                    }
                    String productName = product.productName();
                    String productCode = product.productCode();
                    BigDecimal unitPrice = product.unitPrice();

                    BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(info.quantity()));

                    return FranchiseReturnResponse.builder()
                            .returnCode(info.returnCode())
                            .status(info.status())
                            .unitPrice(unitPrice)
                            .franchiseOrderId(info.franchiseOrderId())
                            .quantity(info.quantity())
                            .type(info.type())
                            .requestedDate(info.requestedDate())
                            .boxCode(boxCode)
                            .serialCode(serialCode)
                            .orderCode(orderCode)
                            .productCode(productCode)
                            .productName(productName)
                            .totalPrice(totalPrice)
                            .build();
                })
                .toList();
    }
}