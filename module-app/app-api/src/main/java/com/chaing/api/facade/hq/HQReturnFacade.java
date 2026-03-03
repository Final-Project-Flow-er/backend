package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.response.HQReturnResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.info.ReturnItemInfo;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.dto.response.ReturnAndOrderInfo;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.service.FakeReturnFranchiseService;
import com.chaing.domain.returns.service.FranchiseReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
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
        // 2. Map<orderItemId, serialCode>
        Map<Long, String> serialCodeByOrderItemId = franchiseOrderService.getSerialCodesByOrderItemId(orderItemIds);
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
}
