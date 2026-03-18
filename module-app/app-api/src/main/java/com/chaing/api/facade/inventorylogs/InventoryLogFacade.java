package com.chaing.api.facade.inventorylogs;

import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.info.InboundProductIdInfo;
import com.chaing.domain.inventories.dto.info.StockInfoForLog;
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
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.service.FranchiseReturnService;
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

    private static final Long FACTORY_ID = 1L;
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
        Long quantity = serialCodes.stream().distinct().count();


        Map<Long, OrderInfoForLog> orderInfos;

        switch (role) {
            case "FRANCHISE":
                inventoryLogInfo = inventoryService.getStockBySerialCodeFromFranchise(serialCodes, unitId);
                orderIds = inventoryLogInfo.stream()
                        .map(StockInfoForLog::orderId)
                        .distinct()
                        .toList();

                if (orderIds.isEmpty()) {
                    throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_ID);
                }
                orderInfos = franchiseOrderService.getOrderInfoForLog(orderIds);
                break;

            case "FACTORY":
                inventoryLogInfo = inventoryService.getStockBySerialCodeFromFactory(serialCodes);
                orderIds = inventoryLogInfo.stream()
                        .map(StockInfoForLog::orderId)
                        .filter(orderId -> orderId != null)
                        .distinct()
                        .toList();

                if (orderIds.isEmpty()) {
                    List<InboundProductIdInfo> inboundProductIdInfos = inventoryService.getProductIdFromFactory(serialCodes);

                    List<Long> productIds = inboundProductIdInfos.stream()
                            .map(InboundProductIdInfo::productId)
                            .toList();

                    Map<Long, ProductInfo> productInfoMap = productIds.isEmpty()
                            ? Map.of()
                            : productService.getProductInfos(productIds);

                    List<InventoryLogCreateRequest> logs = new ArrayList<>();

                    for(InboundProductIdInfo info : inboundProductIdInfos){
                        InventoryLogCreateRequest log = new InventoryLogCreateRequest(
                                info.productId(),
                                productInfoMap.get(info.productId()).productName(),
                                "",
                                "",
                                status,
                                info.quantity().intValue(),
                                LocationType.FACTORY,
                                FACTORY_ID,
                                LocationType.FACTORY,
                                FACTORY_ID,
                                ActorType.FACTORY,
                                FACTORY_ID
                        );
                        logs.add(log);
                    }
                    if (!logs.isEmpty()) {
                        inventoryLogService.recordInventoryLog(logs);
                    }
                    return;
                }

                orderInfos = franchiseOrderService.getOrderInfoForLog(orderIds);
                break;

            default:
                throw new InventoryLogException(InventoryLogtErrorCode.INVALID_ACTOR_TYPE);
        }

        List<Long> productIds = inventoryLogInfo.stream()
                .map(StockInfoForLog::productId)
                .distinct()
                .toList();

        Map<Long, ProductInfo> productInfoMap = productIds.isEmpty()
                ? Map.of()
                : productService.getProductInfos(productIds);

        List<String> boxCodes = inventoryLogInfo.stream()
                .map(StockInfoForLog::boxCode)
                .filter(boxCode -> boxCode != null && !boxCode.isBlank())
                .distinct()
                .toList();

        // 박스당 속해있는 수
        Map<String, Long> quantityByBoxCode = switch (role) {
            case "FRANCHISE" -> inventoryService.getFranchiseQuantityByBoxCodes(unitId, boxCodes);
            case "FACTORY" -> inventoryService.getFactoryQuantityByBoxCodes(boxCodes);
            default -> Map.of();
        };

        List<InventoryLogCreateRequest> logs = new ArrayList<>();

        for (StockInfoForLog stock : inventoryLogInfo) {
            OrderInfoForLog orderInfo = orderInfos.get(stock.orderId());
            if (orderInfo == null) {
                throw new InventoryLogException(InventoryLogtErrorCode.ORDER_NOT_FOUND);
            }

            ProductInfo productInfo = productInfoMap.get(stock.productId());
            String productName = productInfo != null ? productInfo.productName() : "알 수 없는 상품";

            InventoryLogCreateRequest log = switch (role) {
                case "FRANCHISE" -> new InventoryLogCreateRequest(
                        stock.productId(),
                        productName,
                        stock.boxCode(),
                        orderInfo.orderCode(),
                        status,
                        quantityByBoxCode.getOrDefault(stock.boxCode(),1L).intValue(),
                        LocationType.FACTORY,
                        FACTORY_ID,
                        LocationType.FRANCHISE,
                        orderInfo.toId(),
                        parseActorType(role),
                        unitId
                );
                case "FACTORY" -> new InventoryLogCreateRequest(
                        stock.productId(),
                        productName,
                        stock.boxCode() != null ? stock.boxCode() : "",
                        orderInfo.orderCode(),
                        status,
                        quantityByBoxCode.getOrDefault(stock.boxCode(),1L).intValue(),
                        LocationType.FACTORY,
                        FACTORY_ID,
                        LocationType.FACTORY,
                        FACTORY_ID,
                        parseActorType(role),
                        unitId
                );
                default -> throw new InventoryLogException(InventoryLogtErrorCode.INVALID_ACTOR_TYPE);
            };

            logs.add(log);
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
                    actorType == ActorType.HQ ? FACTORY_ID : returns.getFranchiseId(),
                    actorType == ActorType.HQ ? LocationType.FRANCHISE : LocationType.HQ,
                    actorType == ActorType.HQ ? returns.getFranchiseId() : FACTORY_ID,
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
