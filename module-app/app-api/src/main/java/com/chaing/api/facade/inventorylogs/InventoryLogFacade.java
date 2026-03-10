package com.chaing.api.facade.inventorylogs;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.inventorylogs.dto.request.InventoryLogCreateRequest;
import com.chaing.domain.inventorylogs.dto.response.BoxCodeResponse;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.enums.LocationType;
import com.chaing.domain.inventorylogs.exception.InventoryLogException;
import com.chaing.domain.inventorylogs.exception.InventoryLogtErrorCode;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.orders.service.HQOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.service.FranchiseReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryLogFacade {

    private final InventoryLogService inventoryLogService;
    private final HQOrderService hqOrderService;
    private final FranchiseOrderService franchiseOrderService;
    private final FranchiseReturnService franchiseReturnService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public void recordOrderLogs(Long orderId, String orderType, LogType logType, ActorType actorType, Long actorId) {
        if ("FRANCHISE".equalsIgnoreCase(orderType)) {
            FranchiseOrder franchiseOrder = franchiseOrderService.getOrderByOrderId(orderId);
            List<FranchiseOrderItem> franchiseItems = franchiseOrderService.getFranchiseOrderItemsByOrderId(orderId);
            List<FranchiseInventory> franchiseInventories = inventoryService.getFranchiseInventoriesByOrderId(orderId);

            recordFranchiseOrderLogs(franchiseOrder, franchiseItems, franchiseInventories, logType, actorType, actorId);
        } else if ("HQ".equalsIgnoreCase(orderType)) {
            HeadOfficeOrder hqOrder = hqOrderService.getOrderByOrderId(orderId);
            List<HeadOfficeOrderItem> hqItems = hqOrderService.getOrderItemsByOrderId(orderId);
            List<FactoryInventory> factoryInventories = inventoryService.getFactoryInventoriesByOrderId(orderId);

            recordHqOrderLogs(hqOrder, hqItems, factoryInventories, logType, actorType, actorId);
        } else {
            throw new InventoryLogException(InventoryLogtErrorCode.INVALID_ACTOR_TYPE);
        }
    }

    private void recordHqOrderLogs(HeadOfficeOrder order, List<HeadOfficeOrderItem> items,
            List<FactoryInventory> inventories, LogType logType,
            ActorType actorType, Long actorId) {

        List<Long> productIds = items.stream().map(HeadOfficeOrderItem::getProductId).toList();
        Map<Long, ProductInfo> productInfos = productService.getProductInfos(productIds);

        List<InventoryLogCreateRequest> logs = new ArrayList<>();

        for (FactoryInventory inv : inventories) {
            ProductInfo pInfo = productInfos.get(inv.getProductId());
            logs.add(new InventoryLogCreateRequest(
                    inv.getProductId(),
                    pInfo != null ? pInfo.productName() : "알 수 없는 상품",
                    inv.getBoxCode(),
                    order.getOrderCode(),
                    logType,
                    1,
                    null, null,
                    LocationType.FACTORY, null,
                    LocationType.HQ, order.getHqId(),
                    actorType,
                    actorId));
        }

        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }
    }

    private void recordFranchiseOrderLogs(FranchiseOrder order, List<FranchiseOrderItem> items,
            List<FranchiseInventory> inventories, LogType logType,
            ActorType actorType, Long actorId) {

        List<Long> productIds = items.stream().map(FranchiseOrderItem::getProductId).toList();
        Map<Long, ProductInfo> productInfos = productService.getProductInfos(productIds);

        List<InventoryLogCreateRequest> logs = new ArrayList<>();

        for (FranchiseInventory inv : inventories) {
            ProductInfo pInfo = productInfos.get(inv.getProductId());
            logs.add(new InventoryLogCreateRequest(
                    inv.getProductId(),
                    pInfo != null ? pInfo.productName() : "알 수 없는 상품",
                    inv.getBoxCode(),
                    order.getOrderCode(),
                    logType,
                    1,
                    null, null,
                    LocationType.FACTORY, null,
                    LocationType.FRANCHISE, order.getFranchiseId(),
                    actorType,
                    actorId));
        }

        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }
    }

    public void recordReturnLogs(Long returnId, LogType logType, ActorType actorType, Long actorId) {
        // 1. 반품 정보 조회 (returnCode, franchiseId, franchiseOrderId 포함)
        Returns returns = franchiseReturnService.getReturnByReturnId(returnId);

        // 2. 반품 아이템 조회 (boxCode 포함)
        List<ReturnItem> returnItems = franchiseReturnService.getReturnItemListByReturnId(returnId);

        // 3. 원래 발주의 FranchiseInventory에서 boxCode → productId 매핑
        List<FranchiseInventory> inventories = inventoryService
                .getFranchiseInventoriesByOrderId(returns.getFranchiseOrderId());

        // Map<boxCode, productId>
        Map<String, Long> productIdByBoxCode = inventories.stream()
                .filter(inv -> inv.getBoxCode() != null)
                .collect(Collectors.toMap(
                        FranchiseInventory::getBoxCode,
                        FranchiseInventory::getProductId,
                        (existing, replacement) -> existing // 중복 boxCode 시 첫 번째 값 사용
                ));

        // 4. productName 조회
        List<Long> productIds = productIdByBoxCode.values().stream().distinct().toList();
        Map<Long, ProductInfo> productInfos = productIds.isEmpty() ? Map.of()
                : productService.getProductInfos(productIds);

        // 5. 로그 생성
        List<InventoryLogCreateRequest> logs = new ArrayList<>();

        for (ReturnItem item : returnItems) {
            Long productId = productIdByBoxCode.get(item.getBoxCode());
            if (productId == null) {
                continue;
            }
            ProductInfo pInfo = productId != null ? productInfos.get(productId) : null;

            logs.add(new InventoryLogCreateRequest(
                    productId,
                    pInfo != null ? pInfo.productName() : "알 수 없는 상품",
                    item.getBoxCode(),
                    returns.getReturnCode(), // 반품 코드를 transactionCode로 사용
                    logType,
                    1,
                    null, null,
                    LocationType.FRANCHISE, returns.getFranchiseId(), // 출발지: 가맹점
                    LocationType.HQ, null, // 도착지: 본사
                    actorType,
                    actorId));
        }

        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }
    }

    public List<BoxCodeResponse> getBoxCodes(String transactionCode) {
        return inventoryLogService.findBoxCodesByTransactionCode(transactionCode);
    }
}
