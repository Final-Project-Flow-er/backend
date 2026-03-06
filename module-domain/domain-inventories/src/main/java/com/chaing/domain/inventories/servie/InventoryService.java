package com.chaing.domain.inventories.servie;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.InventoryBoxRequest;
import com.chaing.domain.inventories.dto.request.InventoryRequest;
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
import com.chaing.domain.inventories.repository.FactoryInventoryRepository;
import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import com.chaing.domain.inventories.repository.HQInventoryRepository;
import com.chaing.domain.inventories.repository.InventoryPolicyRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
                .findByLocationTypeAndLocationIdAndProductId(
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
                .defaultSafetyStock(policy.getDefaultSafetyStock())
                .safetyStock(safetyStock)
                .build();

        inventoryPolicyRepository.save(policy);
    }

    public List<SafetyStockResponse> getLowStockAlerts(String locationType, Long locationId) {
        return inventoryPolicyRepository.getLowStockAlerts(locationType, locationId);

    }

    // 유통기한
    public List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId) {
        return factoryInventoryRepository.getExpirationAlerts(locationType, locationId);
    }

    // 배송 중 상태 변경
    public void updateShippingStatus(List<InventoryBoxRequest> boxes) {
        // boxes에 있는 모든 seralCode 조회
        List<String> serialCode = convertsSerialCode(boxes);
        // 해당 seralCode 배송 중으로 변경
        factoryInventoryRepository.updateStatus(serialCode, LogType.SHIPPING);
    }

    public void updateFranchiseShippingStatus(Long franchiseId, List<InventoryBoxRequest> boxes) {
        List<String> serialCode = convertsSerialCode(boxes);
        franchiseInventoryRepository.updateFranchiseStatus(franchiseId, serialCode, LogType.SHIPPING);
    }

    // 가맹점 상품 증가
    public void franchiseIncreaseInventory(@Valid InventoryBatchRequest request) {
        List<FranchiseInventory> inventories = request.boxes().stream()
                .flatMap(box -> box.productList().stream()
                        .map(product -> FranchiseInventory.builder()
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

    public void factoryIncreaseInventory(@Valid InventoryBatchRequest request) {
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

    public void hqIncreaseInventory(@Valid InventoryBatchRequest request) {
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

    public void deleteFranchiseInventory(Long franchiseId, List<InventoryBoxRequest> boxes) {
        List<String> serialCode = convertsSerialCode(boxes);
        franchiseInventoryRepository.deleteFranchiseInventory(franchiseId, serialCode);
    }

    public void deleteFactoryInventory(List<InventoryBoxRequest> boxes) {
        List<String> serialCode = convertsSerialCode(boxes);
        factoryInventoryRepository.deleteFactoryInventory(serialCode);
    }

    public void deleteHqInventory(List<InventoryBoxRequest> boxes) {
        List<String> serialCode = convertsSerialCode(boxes);
        hqInventoryRepository.deleteHQInventory(serialCode);
    }

    // 제품 식별코드 반환
    public List<String> convertsSerialCode(List<InventoryBoxRequest> boxes) {
        return boxes.stream()
                .flatMap(box -> box.productList().stream())
                .map(InventoryRequest::serialCode)
                .toList();
    }

}
