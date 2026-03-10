package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.response.HQReturnProductResponse;
import com.chaing.api.dto.hq.response.HQReturnResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.ReturnItemStatus;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.command.HQReturnDetailCommand;
import com.chaing.domain.returns.dto.command.ReturnCommand;
import com.chaing.domain.returns.dto.command.ReturnItemInspection;
import com.chaing.domain.returns.dto.request.HQReturnItemUpdateRequest;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnItemDetailResponse;
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

import java.util.List;
import java.util.Map;
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
    public List<HQReturnResponse> getAllReturns(Boolean isAll) {
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
        // Map<returnItemId, returnItemInspection>
        Map<Long, ReturnItemInspection> returnItemInspectionByReturnItemId = franchiseReturnService.getReturnItemInspection(returnItemIds);

        // 재고 정보 조회
        // Map<serialCode, orderItemId>
        Map<String, Long> orderItemIdBySerialCode = inventoryService.getSerialCodesByOrderItemIds(orderItemIds);
        // List<serialCode>
        List<String> serialCodes = orderItemIdBySerialCode.keySet().stream().toList();
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

        // Map<orderItemId, returnItemId>
        Map<Long, Long> returnItemIdByOrderItemId = orderItemIdByReturnItemId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                ));

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
        if (inventoryService.verifyOmission(requestedBoxCodes)) {
            throw new HQOrderException(HQOrderErrorCode.DATA_OMISSION);
        }

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
        boolean allInspected = !isInspectedBySerialCodeRequest.containsValue(false);
        boolean hasDefective = returnItemStatusByBoxCode.values().stream()
                .anyMatch(ReturnItemStatus.DEFECTIVE::equals);

        if (allInspected) {
            if (hasDefective) {
                // ReturnItem의 returnItemStatus DEFECTIVE로 수정
                franchiseReturnService.updateAllReturnItemByStatus(requestedBoxCodes, ReturnItemStatus.DEFECTIVE);
            } else {
                // ReturnItem의 returnItemStatus NORMAL로 수정
                franchiseReturnService.updateAllReturnItemByStatus(requestedBoxCodes, ReturnItemStatus.NORMAL);
            }
        } else {
            // 검수된 것만 반영
            franchiseReturnService.updateReturnItemByStatus(requestedBoxCodes, returnItemStatusByBoxCode);
        }

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
                .inspectionBySerialCode(itemInspections)
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
