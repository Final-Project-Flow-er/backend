package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.response.HQReturnProductResponse;
import com.chaing.api.dto.hq.response.HQReturnResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.info.ReturnItemInfo;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.command.HQReturnDetailCommand;
import com.chaing.domain.returns.dto.command.ReturnItemInspection;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnItemDetailResponse;
import com.chaing.domain.returns.dto.response.HQReturnDetailResponse;
import com.chaing.domain.returns.dto.response.HQReturnUpdateResponse;
import com.chaing.domain.returns.dto.response.ReturnAndOrderInfo;
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.enums.ReturnItemStatus;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.service.FakeReturnFranchiseService;
import com.chaing.domain.returns.service.FranchiseReturnService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQReturnFacade {

    private final InventoryService inventoryService;
    private final FakeReturnFranchiseService franchiseService;
    private final ProductService productService;
    private final FranchiseReturnService franchiseReturnService;
    private final FranchiseOrderService franchiseOrderService;

    // 반품 요청 조회
    public List<HQReturnResponse> getAllReturns(String username, Boolean isAccepted) {
        Map<Long, HQReturnCommand> returnByReturnCode;
        Map<Long, List<ReturnAndOrderInfo>> returnItemByReturnId;

        if (!isAccepted) {
            // 대기 상태 반품 요청 조회

            // Map<returnId, HQReturnCommand> 반품 정보 조회
            returnByReturnCode = franchiseReturnService.getAllReturnByStatus(ReturnStatus.PENDING);

            // Map<returnId, List<ReturnAndOrderInfo>> 반품 제품 정보 조회
            returnItemByReturnId = franchiseReturnService.getAllReturnItemByStatus(ReturnStatus.PENDING);
        } else {
            // 대기 상태가 아닌 반품 요청 조회

            // Map<returnId, HQReturnCommand> 반품 정보 조회
            returnByReturnCode = franchiseReturnService.getAllNotPendingReturn();

            // Map<returnId, List<ReturnAndOrderInfo>> 반품 제품 정보 조회
            returnItemByReturnId = franchiseReturnService.getAllNotPendingReturnItem();
        }

        // serialCode 조회
        // 1. orderItemIds 추출
        List<Long> orderItemIds = returnItemByReturnId.values().stream()
                .flatMap(List::stream)
                .map(ReturnAndOrderInfo::orderItemId)
                .toList();
        log.info("orderItemIds: {}", orderItemIds);
        // 2. Map<orderItemId, serialCode>
        Map<Long, String> serialCodeByOrderItemId = franchiseOrderService.getSerialCodesByOrderItemId(orderItemIds);
        log.info("serialCodeByOrderItemId: {}", serialCodeByOrderItemId);
        // 3. Map<returnId, List<serialCode>>
        Map<Long, List<String>> serialCodeByReturnId = returnItemByReturnId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry ->
                                entry.getValue().stream()
                                        .map(item -> serialCodeByOrderItemId.get(item.orderItemId()))
                                        .filter(Objects::nonNull)
                                        .toList()
                ));
        log.info("serialCodeByReturnId: {}", serialCodeByReturnId);

        // Map<serialCode, returnItemCommand> boxCode 조회
        List<String> serialCodes = serialCodeByReturnId.values().stream()
                .flatMap(List::stream)
                .toList();
        log.info("serialCodes: {}", serialCodes);
        Map<String, ReturnItemInfo> returnItemBySerialCode = inventoryService.getAllReturnItemInfoBySerialCode(serialCodes);
        if (returnItemBySerialCode == null || returnItemBySerialCode.isEmpty()) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.INVENTORY_NOT_FOUND);
        }

        // Map<returnId, franchiseId> 추출
        Map<Long, Long> franchiseIdByReturnId = returnByReturnCode.values().stream()
                .collect(Collectors.toMap(
                        HQReturnCommand::returnId,
                        HQReturnCommand::franchiseId
                ));
        // Map<returnId, franchiseCode> 가맹점 정보 조회
        Map<Long, String> franchiseCodeByReturnId = franchiseService.getFranchiseCodes(franchiseIdByReturnId);

        // 제품 정보 조회
        // 1. List<productId>
        List<Long> productIds = returnItemBySerialCode.values().stream()
                .map(ReturnItemInfo::productId)
                .distinct()
                .toList();
        // 2. Map<productId, productInfo>
        Map<Long, ProductInfo> productInfoByproductId = productService.getProductInfos(productIds);

        // 반환
        return returnByReturnCode.values().stream()
                .map(returnCommand -> {
                    log.info("returnCommand: {}", returnCommand);
                    log.info("returnId: {}", returnCommand.returnId());
                    log.info("serialCodeByReturnId: {}", serialCodeByReturnId);
                    String serialCode = serialCodeByReturnId.get(returnCommand.returnId()).get(0);
                    Long productId = returnItemBySerialCode.get(serialCode).productId();
                    ProductInfo productInfo = productInfoByproductId.get(productId);
                    String franchiseCode = franchiseCodeByReturnId.get(returnCommand.returnId());

                    return HQReturnResponse.builder()
                            .franchiseCode(franchiseCode)
                            .requestedDate(returnCommand.requestedDate())
                            .returnCode(returnCommand.returnCode())
                            .status(returnCommand.status())
                            .productCode(productInfo.productCode())
                            .type(returnCommand.type())
                            .quantity(returnCommand.quantity())
                            .totalPrice(returnCommand.totalPrice())
                            .receiver(returnCommand.receiver())
                            .phoneNumber(returnCommand.phoneNumber())
                            .boxCode(returnItemBySerialCode.get(serialCodeByReturnId.get(returnCommand.returnId()).get(0)).boxCode())
                            .build();
                })
                .toList();
    }

    // 특정 반품 조회
    public HQReturnDetailResponse getReturn(String username, String returnCode) {
        // 반품 정보 조회
        HQReturnDetailCommand returnInfo = franchiseReturnService.getHQReturnInfo(returnCode);

        // 발주 코드 조회
        String orderCode = franchiseOrderService.getOrderCodeByOrderId(returnInfo.franchiseOrderId());

        // franchiseCode 조회
        String franchiseCode = franchiseService.getFranchise(returnInfo.franchiseId());

        // 연관 발주 제품 정보 조회
        // Map<returnItemId, orderItemId>
        Map<Long, Long> orderItemIdByReturnItemId = franchiseReturnService.getReturnItemId(returnCode);
        List<Long> orderItemIds = orderItemIdByReturnItemId.values().stream().toList();
        List<Long> returnItemIds = orderItemIdByReturnItemId.keySet().stream().toList();
        // Map<returnItemId, returnItemInspection>
        Map<Long, ReturnItemInspection> returnItemInspectionByReturnItemId = franchiseReturnService.getReturnItemInspection(returnItemIds);

        // 재고 정보 조회
        // Map<orderItemId, serialCode>
        Map<Long, String> serialCodeByOrderItemId = franchiseOrderService.getSerialCodesByOrderItemId(orderItemIds);
        List<String> serialCodes = serialCodeByOrderItemId.values().stream().toList();
        // Map<boxCode, serialCode>
        Map<String, String> boxCodeBySerialCode = inventoryService.getBoxCode(serialCodes);
        // Map<serialCode, productId>
        Map<String, Long> productIdBySerialCode = inventoryService.getProductIdBySerialCode(serialCodes);
        log.info("productIdBySerialCode: {}", productIdBySerialCode);

        // 제품 정보 조회
        List<Long> productIds = productIdBySerialCode.values().stream().toList();
        log.info("productIds: {}", productIds);
        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);
        log.info("productInfoByProductId: {}", productInfoByProductId);

        // 역방향 Map
        // Map<serialCode, orderItemId>
        Map<String, Long> orderItemIdBySerialCode = serialCodeByOrderItemId.entrySet().stream()
                .collect(Collectors.toMap(
                                Map.Entry::getValue, Map.Entry::getKey
                        )
                );
        // Map<orderItemId, returnItemId>
        Map<Long, Long> returnItemIdByOrderItemId = orderItemIdByReturnItemId.entrySet().stream()
                .collect(Collectors.toMap(
                                Map.Entry::getValue, Map.Entry::getKey
                        )
                );

        // 반품 제품 DTO
        List<FranchiseReturnItemDetailResponse> items = boxCodeBySerialCode.entrySet().stream()
                .map(entry -> {
                    log.info("entry: {}", entry);
                    Long productId = productIdBySerialCode.get(entry.getKey());
                    log.info("productId: {}", productId);
                    ProductInfo productInfo = productInfoByProductId.get(productId);
                    log.info("productInfo: {}", productInfo);

                    Long orderItemId = orderItemIdBySerialCode.get(entry.getKey());
                    Long returnItemId = returnItemIdByOrderItemId.get(orderItemId);
                    ReturnItemInspection inspection = returnItemInspectionByReturnItemId.get(returnItemId);

                    if (productInfo == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return FranchiseReturnItemDetailResponse.builder()
                            .boxCode(entry.getValue())
                            .serialCode(entry.getKey())
                            .isInspected(inspection.isInspected())
                            .status(inspection.status())
                            .build();
                })
                .toList();

        // 반환
        return HQReturnDetailResponse.builder()
                .returnCode(returnCode)
                .orderCode(orderCode)
                .franchiseCode(franchiseCode)
                .requestedDate(returnInfo.requestedDate())
                .username(returnInfo.receiver())
                .phoneNumber(returnInfo.phoneNumber())
                .type(returnInfo.type())
                .status(returnInfo.status())
                .description(returnInfo.description())
                .totalAmount(returnInfo.totalPrice())
                .items(items)
                .build();
    }

    // 반품 요청 제품 검수
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQReturnUpdateResponse updateReturn(String returnCode, @Valid List<HQReturnUpdateRequest> request) {
        // 반품 조회
        HQReturnDetailCommand returnInfo = franchiseReturnService.getHQReturnInfo(returnCode);

        // 반품 제품 업데이트
        // Map<returnItemId, orderItemId>
        Map<Long, Long> orderItemIdByReturnItemId = franchiseReturnService.getReturnItemId(returnCode);
        // Map<orderItemId, serialCode>
        List<Long> orderItemIds = orderItemIdByReturnItemId.values().stream().toList();
        Map<Long, String> serialCodeByOrderItemId = franchiseOrderService.getSerialCodesByOrderItemId(orderItemIds);
        // Map<returnItemId, serialCode>
        Map<Long, String> serialCodeByReturnItemId = orderItemIdByReturnItemId.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> serialCodeByOrderItemId.get(entry.getValue())
                        ));
        // Map<returnItemId, ReturnItemInspection>
        Map<Long, ReturnItemInspection> inspections = franchiseReturnService.updateReturnItemStatus(serialCodeByReturnItemId, request);

        // 정산
        boolean isProductNormal = inspections.values().stream()
                .noneMatch(inspection -> inspection.status().equals(ReturnItemStatus.DEFECTIVE));

        if (returnInfo.type().equals(ReturnType.MISORDER)) {
            if (isProductNormal) {
                // 경고 1회 추가
                // 정산 취소
            } else {
                // 경고 1회 추가
            }
        } else {
            if (isProductNormal) {
                // 경고 1회 추가
            } else {
                // 정산 취소
            }
        }

        // 반환 DTO
        // Map<serialCode, ReturnItemInspection>
        Map<String, ReturnItemInspection> inspectionBySerialCode = serialCodeByReturnItemId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        entry -> inspections.get(entry.getKey())
                ));

        // 반환
        return HQReturnUpdateResponse.builder()
                .returnId(returnInfo.returnId())
                .returnCode(returnCode)
                .inspectionBySerialCode(inspectionBySerialCode)
                .build();
    }

    // 반품 요청 상태 변경
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<HQReturnProductResponse> updateReturnStatus(List<@NotBlank String> returnCodes) {
        // 반품 상태 변경
        List<ReturnInfo> updatedReturns = franchiseReturnService.updateReturnStatus(returnCodes);

        // 반환
        return updatedReturns.stream()
                .map(HQReturnProductResponse::from)
                .toList();
    }
}
