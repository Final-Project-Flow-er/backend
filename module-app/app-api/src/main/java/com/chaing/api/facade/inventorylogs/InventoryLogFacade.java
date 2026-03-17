package com.chaing.api.facade.inventorylogs;

import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.info.StockInfoForLog;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.inventorylogs.dto.request.InventoryLogCreateRequest;
import com.chaing.domain.inventorylogs.dto.response.BoxCodeResponse;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.enums.LocationType;
import com.chaing.domain.inventorylogs.exception.InventoryLogException;
import com.chaing.domain.inventorylogs.exception.InventoryLogtErrorCode;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import com.chaing.domain.orders.dto.info.OrderInfoForLog;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.service.FranchiseReturnService;
import com.chaing.domain.users.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryLogFacade {

    private static final Long HQ_ID = 1L;
    private final InventoryLogService inventoryLogService;
    private final FranchiseReturnService franchiseReturnService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final FranchiseOrderService franchiseOrderService;

    // orderType: FRANCHISE | FACTORY
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void recordOrderLogs(List<String> serialCodes, UserPrincipal userInfo, LogType status) {

        Long unitId = userInfo.getBusinessUnitId();
        String role = userInfo.getRole().name();

        List<StockInfoForLog> inventoryLogInfo;
        List<Long> orderIds = new ArrayList<>();

        switch (role) {
            case "FRANCHISE":
                inventoryLogInfo = inventoryService.getStockBySerialCodeFromFranchise(serialCodes, unitId);
                orderIds = inventoryLogInfo.stream().map(StockInfoForLog::orderId).toList();
                if(orderIds == null || orderIds.isEmpty()) {
                    throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_ID);
                }

            case "FACTORY":
                inventoryLogInfo = inventoryService.getStockBySerialCodeFromFactory(serialCodes);
                orderIds = inventoryLogInfo.stream().map(StockInfoForLog::orderId).toList();
                if(orderIds == null || orderIds.isEmpty()) {
                    throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_ID);
                }
        }

        List<OrderInfoForLog> orderInfos = franchiseOrderService.getOrderInfoForLog(orderIds);
    }

    // FRANCHISE 주문: Factory -> Franchise
    // boxCode 기준 1박스 1로그, quantity는 박스 내 개수
    public void recordFranchiseOrderLogs(FranchiseOrder order, List<FactoryInventory> inventories, LogType logType, Long toId, ActorType actorType, Long actorId) {
        Map<String, List<FactoryInventory>> inventoriesByBox = inventories.stream()
                .filter(inv -> inv.getBoxCode() != null && !inv.getBoxCode().isBlank())
                .collect(Collectors.groupingBy(
                        FactoryInventory::getBoxCode,
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<Long> productIds = inventoriesByBox.values().stream()
                .flatMap(List::stream)
                .map(FactoryInventory::getProductId)
                .distinct()
                .toList();
        Map<Long, ProductInfo> productInfos = productIds.isEmpty()
                ? Map.of()
                : productService.getProductInfos(productIds);
        List<InventoryLogCreateRequest> logs = new ArrayList<>();
        for (Map.Entry<String, List<FactoryInventory>> entry : inventoriesByBox.entrySet()) {
            String boxCode = entry.getKey();
            List<FactoryInventory> boxItems = entry.getValue();
            FactoryInventory first = boxItems.get(0);
            int quantity = boxItems.size();
            ProductInfo pInfo = productInfos.get(first.getProductId());
            logs.add(new InventoryLogCreateRequest(
                    first.getProductId(),
                    pInfo != null ? pInfo.productName() : "알 수 없는 상품",
                    boxCode,
                    order.getOrderCode(),
                    logType,
                    quantity,
                    LocationType.FACTORY,
                    toId,
                    LocationType.FRANCHISE,
                    order.getFranchiseId(),
                    actorType,
                    actorId));
        }
        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }
    }

    // HQ 주문: Factory -> HQ
    // boxCode 기준 1박스 1로그, quantity는 박스 내 개수
    public void recordHqOrderLogs(HeadOfficeOrder order, List<FranchiseInventory> inventories, LogType logType, Long toId, ActorType actorType, Long actorId) {
        Map<String, List<FranchiseInventory>> inventoriesByBox = inventories.stream()
                .filter(inv -> inv.getBoxCode() != null && !inv.getBoxCode().isBlank())
                .collect(Collectors.groupingBy(
                        FranchiseInventory::getBoxCode,
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<Long> productIds = inventoriesByBox.values().stream()
                .flatMap(List::stream)
                .map(FranchiseInventory::getProductId)
                .distinct()
                .toList();
        Map<Long, ProductInfo> productInfos = productIds.isEmpty()
                ? Map.of()
                : productService.getProductInfos(productIds);
        List<InventoryLogCreateRequest> logs = new ArrayList<>();
        for (Map.Entry<String, List<FranchiseInventory>> entry : inventoriesByBox.entrySet()) {
            String boxCode = entry.getKey();
            List<FranchiseInventory> boxItems = entry.getValue();
            FranchiseInventory first = boxItems.get(0);
            int quantity = boxItems.size();
            ProductInfo pInfo = productInfos.get(first.getProductId());
            logs.add(new InventoryLogCreateRequest(
                    first.getProductId(),
                    pInfo != null ? pInfo.productName() : "알 수 없는 상품",
                    boxCode,
                    order.getOrderCode(),
                    logType,
                    quantity,
                    LocationType.FACTORY,
                    toId,
                    LocationType.HQ,
                    HQ_ID,
                    actorType,
                    actorId));
        }
        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }
    }

    // 반품: Franchise -> HQ
    // boxCode 기준 1박스 1로그, quantity는 같은 박스 반품 아이템 수
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void recordReturnLogs(Long returnId, LogType logType, ActorType actorType, Long actorId) {
        Returns returns = franchiseReturnService.getReturnByReturnId(returnId);
        List<ReturnItem> returnItems = franchiseReturnService.getReturnItemListByReturnId(returnId);
        List<FranchiseInventory> inventories = inventoryService
                .getFranchiseInventoriesByOrderId(returns.getFranchiseOrderId());
        Map<String, Long> productIdByBoxCode = inventories.stream()
                .filter(inv -> inv.getBoxCode() != null && !inv.getBoxCode().isBlank())
                .collect(Collectors.toMap(
                        FranchiseInventory::getBoxCode,
                        FranchiseInventory::getProductId,
                        (existing, replacement) -> existing));
        Map<String, List<ReturnItem>> returnItemsByBox = returnItems.stream()
                .filter(item -> item.getBoxCode() != null && !item.getBoxCode().isBlank())
                .collect(Collectors.groupingBy(
                        ReturnItem::getBoxCode,
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<Long> productIds = productIdByBoxCode.values().stream().distinct().toList();
        Map<Long, ProductInfo> productInfos = productIds.isEmpty()
                ? Map.of()
                : productService.getProductInfos(productIds);
        List<InventoryLogCreateRequest> logs = new ArrayList<>();
        for (Map.Entry<String, List<ReturnItem>> entry : returnItemsByBox.entrySet()) {
            String boxCode = entry.getKey();
            int quantity = (int) inventories.stream()
                    .filter(inv -> boxCode.equals(inv.getBoxCode()))
                    .count();
            if (quantity == 0) {
                quantity = entry.getValue().size();
            }
            Long productId = productIdByBoxCode.get(boxCode);
            if (productId == null) {
                continue;
            }
            ProductInfo pInfo = productInfos.get(productId);
            logs.add(new InventoryLogCreateRequest(
                    productId,
                    pInfo != null ? pInfo.productName() : "알 수 없는 상품",
                    boxCode,
                    returns.getReturnCode(),
                    logType,
                    quantity,
                    actorType == ActorType.HQ ? LocationType.HQ : LocationType.FRANCHISE,
                    actorType == ActorType.HQ ? HQ_ID : returns.getFranchiseId(),
                    actorType == ActorType.HQ ? LocationType.FRANCHISE : LocationType.HQ,
                    actorType == ActorType.HQ ? returns.getFranchiseId() : HQ_ID,
                    actorType,
                    actorId));
        }
        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }
    }

    public List<BoxCodeResponse> getBoxCodes(String transactionCode, LocalDate date) {
        return inventoryLogService.findBoxCodesByTransactionCode(transactionCode, date);
    }

    private ActorType parseActorType(String actorType) {
        try {
            return ActorType.valueOf(actorType.toUpperCase());
        } catch (Exception e) {
            throw new InventoryLogException(InventoryLogtErrorCode.INVALID_ACTOR_TYPE);
        }
    }
}
