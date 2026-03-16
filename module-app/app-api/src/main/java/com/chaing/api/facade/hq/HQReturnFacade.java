package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.response.HQReturnProductResponse;
import com.chaing.api.dto.hq.response.HQReturnResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.ReturnItemStatus;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.command.HQReturnDetailCommand;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.core.dto.info.ReturnItemInspection;
import com.chaing.domain.returns.dto.request.HQOrderStatusUpdateRequest;
import com.chaing.domain.returns.dto.request.HQReturnItemUpdateRequest;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnItemDetailResponse;
import com.chaing.domain.returns.dto.response.HQOrderStatusShippingPendingResponse;
import com.chaing.domain.returns.dto.response.HQReturnDetailResponse;
import com.chaing.domain.returns.dto.response.HQReturnUpdateResponse;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.service.FranchiseReturnService;
import com.chaing.domain.users.service.UserManagementService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQReturnFacade {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final FranchiseReturnService franchiseReturnService;
    private final FranchiseOrderService franchiseOrderService;
    private final FranchiseServiceImpl franchiseService;
    private final UserManagementService userManagementService;

    // 반품 요청 조회
    public List<HQReturnResponse> getAllReturns(boolean isAll) {
        Map<Long, HQReturnCommand> returnByReturnCode;
        if (!isAll) {
            // 대기 상태 반품 요청 조회
            // Map<returnId, HQReturnCommand> 반품 정보 조회
            returnByReturnCode = franchiseReturnService.getAllReturnByStatus(ReturnStatus.PENDING);
        } else {
            // 전체 반품 요청 조회
            // Map<returnId, HQReturnCommand> 반품 정보 조회
            returnByReturnCode = franchiseReturnService.getAllReturn();
        }

        // 반환
        return returnByReturnCode.values().stream()
                .map(returnCommand -> {
                    Long returnId = returnCommand.returnId();
                    Long userId = returnCommand.userId();
                    String receiver = userManagementService.getUsernameByUserId(userId);
                    String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);
                    Long franchiseId = returnCommand.franchiseId();
                    String franchiseCode = franchiseService.getById(franchiseId).businessNumber();

                    return HQReturnResponse.builder()
                            .franchiseCode(franchiseCode)
                            .requestedDate(returnCommand.requestedDate())
                            .returnCode(returnCommand.returnCode())
                            .status(returnCommand.status())
                            .type(returnCommand.type())
                            .quantity(returnCommand.quantity())
                            .totalPrice(returnCommand.totalPrice())
                            .receiver(receiver)
                            .phoneNumber(phoneNumber)
                            .build();
                })
                .toList();
    }

    // 특정 반품 조회
    public HQReturnDetailResponse getReturn(String returnCode) {
        // 반품 정보 조회
        HQReturnDetailCommand returnInfo = franchiseReturnService.getHQReturnInfo(returnCode);

        // franchiseCode
        String franchiseCode = franchiseService.getById(returnInfo.franchiseId()).code();

        // 발주 코드 조회
        String orderCode = franchiseOrderService.getOrderCodeByOrderId(returnInfo.franchiseOrderId());

        // 연관 발주 제품 정보 조회
        // Map<returnItemId, orderItemId>
        Map<Long, Long> orderItemIdByReturnItemId = franchiseReturnService.getReturnItemId(returnCode);
        List<Long> orderItemIds = orderItemIdByReturnItemId.values().stream().toList();
        List<Long> returnItemIds = orderItemIdByReturnItemId.keySet().stream().toList();

        // Map<boxCode, ReturnItemInspection>
        Map<String, ReturnItemInspection> itemInspectionByBoxCode;
        Map<String, Long> orderItemIdBySerialCode;
        List<String> serialCodes;
        Map<String, String> boxCodeBySerialCode;
        Map<String, Long> productIdBySerialCode;

        Set<ReturnStatus> hqStatuses = Set.of(
                ReturnStatus.COMPLETED,
                ReturnStatus.INSPECTING,
                ReturnStatus.DEDUCTION_COMPLETED,
                ReturnStatus.DEDUCTION_REJECTED
                );

        if (!hqStatuses.contains(returnInfo.status())) {
            // FranchiseInventory에 제품 존재
            // 재고 정보 조회
            // Map<serialCode, orderItemId>
            orderItemIdBySerialCode = inventoryService.getSerialCodesByOrderItemIdsFromFranchise(orderItemIds);
            // List<serialCode>
            serialCodes = orderItemIdBySerialCode.keySet().stream().toList();
            // Map<serialCode, boxCode>
            boxCodeBySerialCode = inventoryService.getBoxCodeFromFranchise(serialCodes);
            // Map<serialCode, productId>
            productIdBySerialCode = inventoryService.getProductIdBySerialCodeFromFranchise(serialCodes);
            // Map<boxCode, returnItemInspection>
            itemInspectionByBoxCode = inventoryService.getReturnItemInspectionFromFranchise(orderItemIds);
        } else {
            // HQInventory에 제품 존재
            // 재고 정보 조회
            // Map<serialCode, orderItemId>
            orderItemIdBySerialCode = inventoryService.getSerialCodesByOrderItemIdsFromHQ(orderItemIds);
            // List<serialCode>
            serialCodes = orderItemIdBySerialCode.keySet().stream().toList();
            // Map<serialCode, boxCode>
            boxCodeBySerialCode = inventoryService.getBoxCodeFromHQ(serialCodes);
            // Map<serialCode, productId>
            productIdBySerialCode = inventoryService.getProductIdBySerialCodeFromHQ(serialCodes);
            // Map<boxCode, returnItemInspection>
            itemInspectionByBoxCode = inventoryService.getReturnItemInspection(orderItemIdByReturnItemId);
        }

        log.info("productIdBySerialCode: {}", productIdBySerialCode);

        // 제품 정보 조회
        List<Long> productIds = productIdBySerialCode.values().stream().toList();
        log.info("productIds: {}", productIds);
        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);
        log.info("productInfoByProductId: {}", productInfoByProductId);

        // 반품 제품 DTO
        List<FranchiseReturnItemDetailResponse> items = boxCodeBySerialCode.entrySet().stream()
                .map(entry -> {
                    log.info("entry: {}", entry);
                    Long productId = productIdBySerialCode.get(entry.getKey());
                    log.info("productId: {}", productId);
                    ProductInfo productInfo = productInfoByProductId.get(productId);
                    log.info("productInfo: {}", productInfo);

                    ReturnItemInspection inspection = itemInspectionByBoxCode.get(entry.getValue());

                    if (inspection == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_ITEM_NOT_FOUND);
                    }

                    if (productInfo == null) {
                        throw new FranchiseReturnException(FranchiseReturnErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return FranchiseReturnItemDetailResponse.builder()
                            .boxCode(entry.getValue())
                            .serialCode(entry.getKey())
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
                .type(returnInfo.type())
                .status(returnInfo.status())
                .description(returnInfo.description())
                .totalAmount(returnInfo.totalPrice())
                .items(items)
                .build();
    }

    // 반품 요청 제품 검수
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQReturnUpdateResponse updateReturn(String returnCode, HQReturnUpdateRequest request) {
        // 반품 조회
        HQReturnDetailCommand returns = franchiseReturnService.getHQReturnInfo(returnCode);

        if (returns.status() != ReturnStatus.COMPLETED) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_RETURN_STATUS);
        }

        // List<boxCode>
        List<String> requestedBoxCodes = request.items().stream()
                .map(HQReturnItemUpdateRequest::boxCode)
                .toList();

        // 데이터 누락 검증
        inventoryService.verifyOmission(requestedBoxCodes);

        // 검수 결과 저장
        // Map<boxCode, ReturnItemStatus>
        Map<String, ReturnItemStatus> returnItemStatusByBoxCode = franchiseReturnService.inspectReturnItems(returns.returnId(), request);

        // Map<serialCode, isInspected>
        Map<String, Boolean> isInspectedBySerialCodeRequest = request.items().stream()
                .collect(Collectors.toMap(
                        HQReturnItemUpdateRequest::serialCode,
                        HQReturnItemUpdateRequest::isInspected
                ));

        // 물건 전부 검수되면 ReturnItem의 returnItemStatus 수정
        Map<String, ReturnItemStatus> finalStatusByBoxCode = new HashMap<>();
        returnItemStatusByBoxCode.forEach((boxCode, status) -> {
            if (status == null || boxCode == null) {
                throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_RETURN_STATUS);
            }

            if (status.equals(ReturnItemStatus.DEFECTIVE)) {
                finalStatusByBoxCode.putAll(franchiseReturnService.updateReturnItemByStatus(boxCode, status));
            } else if (status.equals(ReturnItemStatus.NORMAL)) {
                finalStatusByBoxCode.putAll(franchiseReturnService.updateReturnItemByStatus(boxCode, status));
            } else {
                throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_RETURN_STATUS);
            }
        });

        // HQInventory 검수 결과 반영
        inventoryService.saveInspectionResults(requestedBoxCodes, finalStatusByBoxCode, isInspectedBySerialCodeRequest);

        // ReturnStatus 수정
        ReturnStatus updatedStatus = franchiseReturnService.updateReturnStatusInInspection(returns.returnId(), request.returnStatus());

        // 정산

        // Map<returnItemId, ReturnItemCommand>
        Map<Long, ReturnItemInspection> returnItems = franchiseReturnService.getReturnItemsInspection(returns.returnId());

        // List<ReturnItemInspection>
        List<ReturnItemInspection> itemInspections = returnItems.values().stream().toList();

        // 반환
        return HQReturnUpdateResponse.builder()
                .returnCode(returns.returnCode())
                .status(updatedStatus)
                .returnItemInspection(itemInspections)
                .build();
    }

    // 반품 요청 접수
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<HQReturnProductResponse> acceptReturn(List<@NotBlank String> returnCodes) {
        // 반품 요청 접수
        List<ReturnCommand> acceptedReturns = franchiseReturnService.acceptReturn(returnCodes);

        // 반환
        return acceptedReturns.stream()
                .map(HQReturnProductResponse::from)
                .toList();
    }

    // 반품 상태 SHIPPING_PENDING으로 수정
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<HQOrderStatusShippingPendingResponse> updateShippingPending(List<HQOrderStatusUpdateRequest> request) {
        // Set<returnCode>
        Set<String> returnCodes = request.stream().map(HQOrderStatusUpdateRequest::returnCode).collect(Collectors.toSet());

        // 수정
        // Map<returnId, ReturnCommand>
        Map<Long, ReturnCommand> returns = franchiseReturnService.updateShippingPending(returnCodes);

        // 반환
        return returns.values().stream()
                .map(HQOrderStatusShippingPendingResponse::from)
                .toList();
    }
}
