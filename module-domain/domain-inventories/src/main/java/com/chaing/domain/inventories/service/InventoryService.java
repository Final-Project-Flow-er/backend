package com.chaing.domain.inventories.service;

import com.chaing.core.dto.command.FranchiseInventoryCommand;
import com.chaing.core.dto.command.FranchiseOrderCodeAndQuantityCommand;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.core.enums.ReturnItemStatus;
import com.chaing.domain.inventories.dto.request.DisposalRequest;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.InventoryRequest;
import com.chaing.domain.inventories.dto.request.SafetyStockRequest;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockResponse;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.entity.HQInventory;
import com.chaing.domain.inventories.entity.InventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.repository.FactoryInventoryRepository;
import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import com.chaing.domain.inventories.repository.HQInventoryRepository;
import com.chaing.domain.inventories.repository.InventoryPolicyRepository;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final HQInventoryRepository hqInventoryRepository;
    private final FranchiseInventoryRepository franchiseInventoryRepository;
    private final FactoryInventoryRepository factoryInventoryRepository;
    private final InventoryPolicyRepository inventoryPolicyRepository;

    // 대분류
    public Map<Long, InventoryProductInfoResponse> getStock(List<Long> ids, String status) {
        return factoryInventoryRepository.getStock(ids, status);
    }

    // 중분류
    public Page<HQInventoryBatchResponse> getBatches(Long productId, Pageable pageable) {
        return factoryInventoryRepository.getBatches(productId, pageable);
    }
    public Page<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request, Pageable pageable) {
        return factoryInventoryRepository.getItems(request, pageable);
    }


    // 가맹점 대분류
    public Map<Long, InventoryProductInfoResponse> getFranchiseStock(Long franchiseId, List<Long> ids, String status) {
        return franchiseInventoryRepository.getFranchiseStock(franchiseId, ids, status);
    }

    // 가맹점 중분류
    public Page<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId, Pageable pageable) {
        return franchiseInventoryRepository.getFranchiseBatches(franchiseId, productId, pageable);
    }
    public Page<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId, FranchiseInventoryItemsRequest request, Pageable pageable) {
        return franchiseInventoryRepository.getFranchiseItems(franchiseId, request, pageable);
    }

    public List<Long> getAllFranchiseIds() {
        return franchiseInventoryRepository.getAllFranchiseIds();
    }

    // 안전재고 업데이트
    public void updateSafetyStock(LocationType locationType, Long locationId, Long productId, int safetyStock) {

        InventoryPolicy policy = inventoryPolicyRepository
                .findPolicy(
                        locationType,
                        locationId,
                        productId)
                .orElseGet(() -> InventoryPolicy.builder()
                        .locationType(locationType)
                        .locationId(locationId)
                        .productId(productId)
                        .build());

        policy = InventoryPolicy.builder()
                .id(policy.getId())
                .locationType(policy.getLocationType())
                .locationId(policy.getLocationId())
                .productId(policy.getProductId())
                .defaultSafetyStock(safetyStock)
                .safetyStock(policy.getSafetyStock())
                .build();

        inventoryPolicyRepository.save(policy);
    }

    // 수동 안전재고 완전 초기화 (시스템 기본값으로 회귀)
    public void resetSafetyStockToDefault(String locationType, Long locationId, Long productId) {
        InventoryPolicy policy = inventoryPolicyRepository
                .findPolicy(convertLocationType(locationType), locationId, productId)
                .orElseThrow(() -> new InventoriesException(InventoriesErrorCode.DATA_OMISSION));

        policy.updateManualSafetyStock(null);
        inventoryPolicyRepository.save(policy);
    }

    public LocationType convertLocationType(String locationType) {
        try {
            return LocationType.valueOf(locationType);
        } catch (IllegalArgumentException e) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_TYPE);
        }
    }

    // 안전 재고 알림
    public List<SafetyStockResponse> getLowStockAlerts(String locationType, Long locationId) {
        if (locationType.equals("FRANCHISE")) {
            return franchiseInventoryRepository.getLowStockAlerts(locationType, locationId);
        } else {
            return factoryInventoryRepository.getLowStockAlerts(locationType, locationId);
        }
    }

    // 유통기한
    public List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId) {
        if (locationType.equals("FRANCHISE")) {
            return franchiseInventoryRepository.getExpirationAlerts(locationType, locationId);
        } else {
            return factoryInventoryRepository.getExpirationAlerts(locationType, locationId);
        }
    }

    // 유통기한 상태 자동 업데이트
    public void updateExpiredStatus() {

        LocalDate expirationThreshold = LocalDate.now().minusYears(1);

        hqInventoryRepository.updateExpiredStatus(expirationThreshold);
        factoryInventoryRepository.updateExpiredStatus(expirationThreshold);
        franchiseInventoryRepository.updateExpiredStatus(expirationThreshold);
    }

    // 배송 중 상태 변경
    public void updateShippingStatus(List<String> serialCodes) {
        // 해당 seralCode 배송 중으로 변경
        factoryInventoryRepository.updateStatus(serialCodes, LogType.SHIPPING);
    }

    public void updateFranchiseShippingStatus(Long franchiseId, List<String> serialCodes) {
        franchiseInventoryRepository.updateFranchiseStatus(franchiseId, serialCodes, LogType.SHIPPING);
    }

    // 가맹점 상품 증가
    public void franchiseIncreaseInventory(InventoryBatchRequest request) {
        List<FranchiseInventory> inventories = request.boxes().stream()
                .flatMap(box -> box.productList().stream()
                        .map(product -> FranchiseInventory.builder()
                                .orderId(request.orderId())
                                .orderItemId(product.orderItemId())
                                .serialCode(product.serialCode())
                                .productId(product.productId())
                                .manufactureDate(product.manufactureDate())
                                .franchiseId(request.toLocationId()) // 목적지로 재고가 추가되어야 함
                                .status(product.productLogType())
                                .boxCode(box.boxCode())
                                .orderCode(request.transactionCode())
                                .shippedAt(request.shippedAt()) // 배송 완료 시간 추가
                                .build()))
                .toList();
        franchiseInventoryRepository.saveAll(inventories);
    }

    public void factoryIncreaseInventory(InventoryBatchRequest request) {
        List<FactoryInventory> inventories = request.boxes().stream()
                .flatMap(box -> box.productList().stream()
                        .map(product -> FactoryInventory.builder()
                                .serialCode(product.serialCode())
                                .productId(product.productId())
                                .manufactureDate(product.manufactureDate())
                                .status(product.productLogType())
                                .boxCode(box.boxCode())
                                .shippedAt(request.shippedAt()) // 배송 완료 시간 추가
                                .build()))
                .toList();
        factoryInventoryRepository.saveAll(inventories);
    }

    public void hqIncreaseInventory(InventoryBatchRequest request) {
        List<HQInventory> inventories = request.boxes().stream()
                .flatMap(box -> box.productList().stream()
                        .map(product -> HQInventory.builder()
                                .serialCode(product.serialCode())
                                .productId(product.productId())
                                .manufactureDate(product.manufactureDate())
                                .status(product.productLogType())
                                .boxCode(box.boxCode())
                                .shippedAt(request.shippedAt()) // 배송 완료 시간 추가
                                .build()))
                .toList();
        hqInventoryRepository.saveAll(inventories);
    }

    public void deleteFranchiseInventory(Long franchiseId, List<String> serialCodes) {
        franchiseInventoryRepository.deleteFranchiseInventory(franchiseId, serialCodes);
    }

    public void deleteFactoryInventory(List<String> serialCodes) {
        factoryInventoryRepository.deleteFactoryInventory(serialCodes);
    }

    public void deleteHqInventory(List<String> serialCodes) {
        hqInventoryRepository.deleteHQInventory(serialCodes);
    }

    public List<FactoryInventory> getFactoryInventoriesByOrderId(Long orderId) {
        return factoryInventoryRepository.findAllByOrderId(orderId);
    }

    public List<FranchiseInventory> getFranchiseInventoriesByOrderId(Long orderId) {
        return franchiseInventoryRepository.findAllByOrderId(orderId);
    }

    // return: Map<boxCode, serialCode>
    public Map<String, String> getBoxCode(List<String> serialCodes) {
        List<FranchiseInventory> inventories = franchiseInventoryRepository.findAllBySerialCodeIn(serialCodes);

        if (inventories == null || inventories.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        FranchiseInventory::getBoxCode));
    }

    // serialCode로 productId 조회
    // Map<serialCode, productId>
    public Map<String, Long> getProductIdBySerialCode(List<String> serialCodes) {
        List<FranchiseInventory> inventories = franchiseInventoryRepository.findAllBySerialCodeIn(serialCodes);

        if (inventories == null || inventories.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        FranchiseInventory::getProductId));
    }

    // return: Map<serialCode, orderItemId>
    public Map<String, Long> getSerialCodesByOrderItemIds(List<Long> orderItemIds) {
        List<FranchiseInventory> inventories = franchiseInventoryRepository.findAllByOrderItemIdIn(orderItemIds);

        if (inventories == null || inventories.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        FranchiseInventory::getOrderItemId));
    }

    // return: Map<boxCode, FranchiseInventoryCommand>
    public Map<String, FranchiseInventoryCommand> getInventoriesByBoxCode(List<String> boxCodes) {
        List<FranchiseInventory> inventories = franchiseInventoryRepository.findAllByBoxCodeIn(boxCodes);

        if (inventories == null || inventories.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getBoxCode,
                        inventory -> FranchiseInventoryCommand.builder()
                                .inventoryId(inventory.getInventoryId())
                                .orderItemId(inventory.getOrderItemId())
                                .orderId(inventory.getOrderId())
                                .productId(inventory.getProductId())
                                .serialCode(inventory.getSerialCode())
                                .boxCode(inventory.getBoxCode())
                                .build()));
    }

    // return: Map<orderItemId, FranchiseInventoryCommand>
    public Map<Long, FranchiseInventoryCommand> getInventoriesByOrderItemIds(List<Long> orderItemIds) {
        List<FranchiseInventory> inventories = franchiseInventoryRepository.findAllByOrderItemIdIn(orderItemIds);

        if (inventories == null || inventories.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
        }
        log.info("inventories={}", inventories);

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getOrderItemId,
                        inventory -> FranchiseInventoryCommand.builder()
                                .inventoryId(inventory.getInventoryId())
                                .orderItemId(inventory.getOrderItemId())
                                .orderId(inventory.getOrderId())
                                .productId(inventory.getProductId())
                                .serialCode(inventory.getSerialCode())
                                .boxCode(inventory.getBoxCode())
                                .build()));
    }

    public void disposalInventory(DisposalRequest request) {
        if (request.actorType() == null || request.actorType().isBlank()) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_TYPE);
        }
        String actorType = request.actorType().toUpperCase();
        if (actorType.equals("HQ")) {
            hqInventoryRepository.deleteByInventoryIdIn(request.inventoryIds());
        } else if (actorType.equals("FRANCHISE")) {
            if (request.actorId() == null) {
                throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_ID);
            }
            franchiseInventoryRepository.deleteByFranchiseIdAndInventoryIdIn(
                    request.actorId(),
                    request.inventoryIds());
        } else if (actorType.equals("FACTORY")) {
            factoryInventoryRepository.deleteByInventoryIdIn(request.inventoryIds());
        } else {
            throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_TYPE);
        }
    }

    public void setSafetyStock(SafetyStockRequest request) {
        if (request.locationType() == null || request.locationType().isBlank()) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_TYPE);
        }

        LocationType type;
        try {
            type = LocationType.valueOf(request.locationType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_TYPE);
        }

        if (request.productId() == null) {
            throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
        }

        Long locationId = request.locationId();

        // 가맹점은 반드시 locationId 필요
        if (type == LocationType.FRANCHISE && locationId == null) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_LOCATION_ID);
        }

        long updatedCount = inventoryPolicyRepository.updateManualSafetyStock(
                type,
                locationId,
                request.productId(),
                request.safetyStock());

        if (updatedCount == 0) {
            InventoryPolicy policy = InventoryPolicy.builder()
                    .locationType(type)
                    .locationId(locationId)
                    .productId(request.productId())
                    .safetyStock(request.safetyStock())
                    .defaultSafetyStock(0)
                    .build();
            inventoryPolicyRepository.save(policy);
        }
    }


    // return: 데이터 누락 있는지 없는지
    public void verifyOmission(List<String> requestedBoxCodes) {
        List<HQInventory> inventories = hqInventoryRepository.findAllByBoxCodeInAndDeletedAtIsNull(requestedBoxCodes);

        Set<String> boxCodes = inventories.stream()
                .map(HQInventory::getBoxCode)
                .collect(Collectors.toSet());

        if (boxCodes.isEmpty() || boxCodes.size() != requestedBoxCodes.size()) {
            throw new InventoriesException(InventoriesErrorCode.DATA_OMISSION);
        }
    }

    // 제품 검수 결과 저장
    public void saveInspectionResults(
            List<String> boxCodes,
            Map<String, ReturnItemStatus> finalStatusByBoxCode,
            Map<String, Boolean> isInspectedBySerialCode
    ) {
        List<HQInventory> inventories = hqInventoryRepository.findAllByBoxCodeInAndDeletedAtIsNull(boxCodes);

        Set<String> existingBoxCodes = inventories.stream()
                .map(HQInventory::getBoxCode)
                .collect(Collectors.toSet());

        if (existingBoxCodes.isEmpty() || !existingBoxCodes.containsAll(new HashSet<>(boxCodes))) {
            throw new InventoriesException(InventoriesErrorCode.DATA_OMISSION);
        }

        for (HQInventory item : inventories) {
            Boolean isInspected = isInspectedBySerialCode.get(item.getSerialCode());
            ReturnItemStatus returnItemStatus = finalStatusByBoxCode.get(item.getBoxCode());

            if (isInspected == null || returnItemStatus == null) {
                throw new InventoriesException(InventoriesErrorCode.DATA_OMISSION);
            }

            item.updateInspection(isInspected, returnItemStatus);
        }

        hqInventoryRepository.saveAll(inventories);
    }

    public List<HQInventory> getHqInventoriesByIds(List<Long> inventoryIds) {
        return hqInventoryRepository.findByInventoryIdIn(inventoryIds);
    }

    public List<FranchiseInventory> getFranchiseInventoriesByIds(List<Long> inventoryIds) {
        return franchiseInventoryRepository.findByInventoryIdIn(inventoryIds);
    }

    public List<FactoryInventory> getFactoryInventoriesByIds(List<Long> inventoryIds) {
        return factoryInventoryRepository.findByInventoryIdIn(inventoryIds);
    }

    public List<Long> expandInventoryIdsByBoxCode(String actorTypeRaw, List<Long> selectedIds, Long locationId, Long actorId) {
        if (selectedIds == null || selectedIds.isEmpty()) return List.of();

        if ("HQ".equals(actorTypeRaw)) {
            List<HQInventory> selected = getHqInventoriesByIds(selectedIds);
            List<String> boxCodes = selected.stream().map(HQInventory::getBoxCode).filter(Objects::nonNull).distinct().toList();
            if (boxCodes.isEmpty()) return List.of();

            return hqInventoryRepository.findByBoxCodeIn(boxCodes).stream()
                    .map(HQInventory::getInventoryId)
                    .distinct()
                    .toList();
        }

        if ("FACTORY".equals(actorTypeRaw)) {
            List<FactoryInventory> selected = getFactoryInventoriesByIds(selectedIds);
            List<String> boxCodes = selected.stream().map(FactoryInventory::getBoxCode).filter(Objects::nonNull).distinct().toList();
            if (boxCodes.isEmpty()) return List.of();

            return factoryInventoryRepository.findByBoxCodeIn(boxCodes).stream()
                    .map(FactoryInventory::getInventoryId)
                    .distinct()
                    .toList();
        }

        if ("FRANCHISE".equals(actorTypeRaw)) {
            List<FranchiseInventory> selected = getFranchiseInventoriesByIds(selectedIds);
            List<String> boxCodes = selected.stream().map(FranchiseInventory::getBoxCode).filter(Objects::nonNull).distinct().toList();
            if (boxCodes.isEmpty()) return List.of();

            return franchiseInventoryRepository.findByBoxCodeInAndFranchiseId(boxCodes, actorId).stream()
                    .map(FranchiseInventory::getInventoryId)
                    .distinct()
                    .toList();
        }

        throw new IllegalArgumentException("Unsupported actorType: " + actorTypeRaw);
    }

    public void disposalInventoryByIds(String actorTypeRaw, List<Long> ids, Long locationId, Long actorId) {
        if (ids == null || ids.isEmpty()) return;

        if ("HQ".equals(actorTypeRaw)) {
            hqInventoryRepository.deleteByInventoryIdIn(ids);
            return;
        }

        if ("FACTORY".equals(actorTypeRaw)) {
            factoryInventoryRepository.deleteByInventoryIdIn(ids);
            return;
        }

        if ("FRANCHISE".equals(actorTypeRaw)) {
            List<Long> scopedIds = franchiseInventoryRepository.findByInventoryIdInAndFranchiseId(ids, actorId).stream()
                    .map(FranchiseInventory::getInventoryId)
                    .toList();
            if (!scopedIds.isEmpty()) {
                franchiseInventoryRepository.deleteByFranchiseIdAndInventoryIdIn(actorId, scopedIds);
            }
            return;
        }

        throw new IllegalArgumentException("Unsupported actorType: " + actorTypeRaw);
    }

    public void checkStock(List<FranchiseOrderCodeAndQuantityCommand> items, Map<String, ProductInfo> productInfoByProductCode) {
        // Set<productId>
        Set<Long> productIds = items.stream()
                .map(item -> {
                    String productCode = item.productCode();
                    ProductInfo productInfo = productInfoByProductCode.get(productCode);

                    if (productInfo == null) {
                        throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return productInfo.productId();
                })
                .collect(Collectors.toSet());

        // List<FactoryInventory>
        List<FactoryInventory> inventories = factoryInventoryRepository.findAllByProductIdInAndStatusAndDeletedAtIsNull(productIds, LogType.AVAILABLE);

        // Set<productId>
        Set<Long> existingProductIds = inventories.stream().map(FactoryInventory::getProductId).collect(Collectors.toSet());

        if (inventories.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
        }

        if (!existingProductIds.containsAll(productIds)) {
            throw new InventoriesException(InventoriesErrorCode.DATA_OMISSION);
        }

        // Map<productId, List<FactoryInventory>>
        Map<Long, List<FactoryInventory>> stockByProductId = inventories.stream()
                .collect(Collectors.groupingBy(
                        FactoryInventory::getProductId,
                        Collectors.mapping(Function.identity(), Collectors.toList())
                ));

        // Map<productId, totalQuantity>
        Map<Long, Integer> requestedQuantityByProductId = items.stream()
                .collect(Collectors.toMap(
                        item -> productInfoByProductCode.get(item.productCode()).productId(),
                        FranchiseOrderCodeAndQuantityCommand::quantity,
                        Integer::sum
                ));

        // 수량 점검
        stockByProductId.forEach((productId, factoryInventories) -> {
            Integer requestedQuantity = requestedQuantityByProductId.get(productId);
            int existingQuantity = stockByProductId.get(productId).size();

            if (requestedQuantity == null || requestedQuantity > existingQuantity) {
                throw new InventoriesException(InventoriesErrorCode.INVALID_STOCK);
            }
        });
    }

    public List<InventoryRequest> getItemBySerialCode(@NotEmpty(message = "선택된 제품이 존재하지 않습니다.") List<String> serialCodes) {

        List<FranchiseInventory> scannedItems = franchiseInventoryRepository.getAllByStatusAndSerialCode(serialCodes);

        if(scannedItems == null || scannedItems.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
        }

        long requestCount = serialCodes.stream().distinct().count();
        long getCount = scannedItems.stream().distinct().count();

        if(requestCount != getCount) {
            throw new InventoriesException(InventoriesErrorCode.INVALID_STOCK);
        }

        return scannedItems.stream()
                .map(item -> new InventoryRequest(
                        item.getProductId(),
                        item.getSerialCode(),
                        item.getOrderItemId(),
                        item.getStatus(),
                        item.getManufactureDate()
                ))
                .toList();
    }

    public List<Long> getOrderInfo(List<String> selectedList) {
        List<Long> orderIds = factoryInventoryRepository.getOrderIdBySerialCodeIn(selectedList);

        if(orderIds == null || orderIds.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
        }
        return orderIds;
    }
}
