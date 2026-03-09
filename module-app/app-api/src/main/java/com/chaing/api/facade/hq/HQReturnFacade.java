package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.response.HQReturnProductResponse;
import com.chaing.api.dto.hq.response.HQReturnResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
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
import com.chaing.domain.returns.dto.response.ReturnInfo;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.service.FranchiseReturnService;
import com.chaing.domain.users.service.UserManagementService;
import jakarta.validation.Valid;
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
    public List<HQReturnResponse> getAllReturns(String username, Boolean isAccepted) {
        Map<Long, HQReturnCommand> returnByReturnCode;

        if (!isAccepted) {
            // 대기 상태 반품 요청 조회
            // Map<returnId, HQReturnCommand> 반품 정보 조회
            returnByReturnCode = franchiseReturnService.getAllReturnByStatus(ReturnStatus.PENDING);
        } else {
            // 대기 상태가 아닌 반품 요청 조회
            // Map<returnId, HQReturnCommand> 반품 정보 조회
            returnByReturnCode = franchiseReturnService.getAllNotPendingReturn();
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
    public HQReturnDetailResponse getReturn(Long userId, String returnCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // franchiseCode 조회
        String franchiseCode = franchiseService.getById(franchiseId).businessNumber();

        // 반품 정보 조회
        HQReturnDetailCommand returnInfo = franchiseReturnService.getHQReturnInfo(returnCode);

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



        // 수정. return: Map<returnItemId, ReturnItemInspection>
//        Map<Long, ReturnItemInspection> returnItemInspectionByReturnItemId = franchiseReturnService.updateReturnItemStatus(returnCode, request);
        
        // Map<returnItemId, orderItemId>
        Map<Long, Long> orderItemIdByReturnItemId = franchiseReturnService.getReturnItemId(returnCode);
        // Map<orderItemId, serialCode>
        List<Long> orderItemIds = orderItemIdByReturnItemId.values().stream().toList();


        // 정산

        // 반환
        return HQReturnUpdateResponse.builder()

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
