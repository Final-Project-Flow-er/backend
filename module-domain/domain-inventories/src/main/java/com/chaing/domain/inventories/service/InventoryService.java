package com.chaing.domain.inventories.service;

import com.chaing.core.dto.command.FranchiseInventoryCommand;
import com.chaing.core.dto.info.ReturnItemInfo;
import com.chaing.core.dto.request.FranchiseReturnUpdateRequest;
import com.chaing.core.dto.returns.request.ReturnToInventoryRequest;
import com.chaing.core.enums.LogType;
import com.chaing.core.enums.ReturnItemStatus;
import com.chaing.domain.inventories.dto.request.DisposalRequest;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
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
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public List<HQInventoryBatchResponse> getBatches(Long productId) {
        return factoryInventoryRepository.getBatches(productId);
    }

    // 소분류
    public List<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request) {
        return factoryInventoryRepository.getItems(request);
    }

    // 가맹점 대분류
    public Map<Long, InventoryProductInfoResponse> getFranchiseStock(Long franchiseId, List<Long> ids, String status) {
        return franchiseInventoryRepository.getFranchiseStock(franchiseId, ids, status);
    }

    // 가맹점 중분류
    public List<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId) {
        return franchiseInventoryRepository.getFranchiseBatches(franchiseId, productId);
    }

    // 가맹점 소분류
    public List<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId,
            FranchiseInventoryItemsRequest request) {
        return franchiseInventoryRepository.getFranchiseItems(franchiseId, request);
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

    // 안전 재고 알림
    public List<SafetyStockResponse> getLowStockAlerts(String locationType, Long locationId) {
        if (locationType.equals("FRANCHISE")) {
            return franchiseInventoryRepository.getLowStockAlerts(locationType, locationId);
        } else {
            // 본사나 공장이면 ID를 null로 보냄 (ID-less)
            return factoryInventoryRepository.getLowStockAlerts(locationType, null);
        }
    }

    // 유통기한
    public List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId) {
        if (locationType.equals("FRANCHISE")) {
            return franchiseInventoryRepository.getExpirationAlerts(locationType, locationId);
        } else {
            // 본사나 공장이면 ID를 null로 보냄 (ID-less)
            return factoryInventoryRepository.getExpirationAlerts(locationType, null);
        }
    }

    // 유통기한 상태 자동 업데이트
    public void updateExpiredStatus() {
        // 현재 로직상 유통기한은 제조일로부터 1년
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

    // 수정 예정
    public List<ReturnToInventoryRequest> getProducts(List<String> serialCodes) {
        return List.of(
                new ReturnToInventoryRequest(
                        "SerialCode",
                        1L,
                        "BoxCode"));
    }

    public List<Long> getProductsBySerialCodeAndBoxCode(List<FranchiseReturnUpdateRequest> requests) {
        return List.of(1L, 2L);
    }

    // 수정 예정
    public List<String> getSerialCodes(Long franchiseId, @NotBlank String boxCode) {
        return List.of("SerialCode");
    }

    // boxCode, productId 조회
    // return: Map<serialCode, returnItemCommand>
    public Map<String, ReturnItemInfo> getAllReturnItemInfoBySerialCode(List<String> serialCodes) {
        return franchiseInventoryRepository.findAllBySerialCodeIn(serialCodes).stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        item -> ReturnItemInfo.builder()
                                .boxCode(item.getBoxCode())
                                .productId(item.getProductId())
                                .build()));
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

    // return: Map<serialCode, FranchiseInventoryCommand>
    public Map<String, FranchiseInventoryCommand> getInventoriesBySerialCodes(List<String> serialCodes) {
        List<FranchiseInventory> inventories = franchiseInventoryRepository.findAllBySerialCodeIn(serialCodes);

        if (inventories == null || inventories.isEmpty()) {
            throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        inventory -> FranchiseInventoryCommand.builder()
                                .inventoryId(inventory.getInventoryId())
                                .orderItemId(inventory.getOrderItemId())
                                .orderId(inventory.getOrderId())
                                .productId(inventory.getProductId())
                                .serialCode(inventory.getSerialCode())
                                .boxCode(inventory.getBoxCode())
                                .build()));
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

        // 본사(HQ)나 공장(FACTORY)이면 ID를 null로 처리 (ID-less)
        if (type == LocationType.HQ || type == LocationType.FACTORY) {
            locationId = null;
        } else if (type == LocationType.FRANCHISE && locationId == null) {
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

    public List<HQInventory> getHqInventoriesByIds(List<Long> inventoryIds) {
        return hqInventoryRepository.findAllById(inventoryIds);
    }

    public List<FranchiseInventory> getFranchiseInventoriesByIds(List<Long> inventoryIds) {
        return franchiseInventoryRepository.findAllById(inventoryIds);
    }

    public List<FactoryInventory> getFactoryInventoriesByIds(List<Long> inventoryIds) {
        return factoryInventoryRepository.findAllById(inventoryIds);
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

        if (existingBoxCodes.isEmpty() || existingBoxCodes.containsAll(new HashSet<>(boxCodes))) {
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
}
