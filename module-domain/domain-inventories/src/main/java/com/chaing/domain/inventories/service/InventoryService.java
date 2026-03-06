package com.chaing.domain.inventories.service;

import com.chaing.core.dto.command.FranchiseInventoryCommand;
import com.chaing.core.dto.info.ReturnItemInfo;
import com.chaing.core.dto.request.FranchiseReturnUpdateRequest;
import com.chaing.core.dto.returns.request.ReturnToInventoryRequest;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final FranchiseInventoryRepository inventoryRepository;

    // 수정 예정
    public List<ReturnToInventoryRequest> getProducts(List<String> serialCodes) {
        return List.of(
                new ReturnToInventoryRequest(
                        "SerialCode",
                        1L,
                        "BoxCode"
                )
        );
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
        return inventoryRepository.findAllBySerialCodeIn(serialCodes).stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        item -> ReturnItemInfo.builder()
                                .boxCode(item.getBoxCode())
                                .productId(item.getProductId())
                                .build()
                ));
    }

    // return: Map<boxCode, serialCode>
    public Map<String, String> getBoxCode(List<String> serialCodes) {
        List<FranchiseInventory> inventories = inventoryRepository.findAllBySerialCodeIn(serialCodes);

        if (inventories == null || inventories.isEmpty()) {
            // ErrorCode 추가해주세요
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        FranchiseInventory::getBoxCode
                ));
    }

    // serialCode로 productId 조회
    // Map<serialCode, productId>
    public Map<String, Long> getProductIdBySerialCode(List<String> serialCodes) {
        List<FranchiseInventory> inventories = inventoryRepository.findAllBySerialCodeIn(serialCodes);

        if (inventories == null || inventories.isEmpty()) {
            // ErrorCode 추가해 주세요
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        FranchiseInventory::getProductId
                ));
    }

    // return: Map<serialCode, orderItemId>
    public Map<String, Long> getSerialCodesByOrderItemIds(List<Long> orderItemIds) {
        List<FranchiseInventory> inventories = inventoryRepository.findAllByOrderItemIdIn(orderItemIds);

        if (inventories == null || inventories.isEmpty()) {
            // 예외 처리
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getSerialCode,
                        FranchiseInventory::getOrderItemId
                ));
    }

    // return: Map<serialCode, FranchiseInventoryCommand>
    public Map<String, FranchiseInventoryCommand> getInventoriesBySerialCodes(List<String> serialCodes) {
        List<FranchiseInventory> inventories = inventoryRepository.findAllBySerialCodeIn(serialCodes);

        if (inventories == null || inventories.isEmpty()) {
            // 예외 처리
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
                                .build()
                ));
    }

    // return: Map<inventoryId, FranchiseInventoryCommand>
    public Map<Long, FranchiseInventoryCommand> getInventoriesByBoxCode(List<String> boxCodes) {
        List<FranchiseInventory> inventories = inventoryRepository.findAllByBoxCodeIn(boxCodes);

        if (inventories == null || inventories.isEmpty()) {
            // 예외 코드 추가
        }

        return inventories.stream()
                .collect(Collectors.toMap(
                        FranchiseInventory::getInventoryId,
                        inventory -> FranchiseInventoryCommand.builder()
                                .inventoryId(inventory.getInventoryId())
                                .orderItemId(inventory.getOrderItemId())
                                .orderId(inventory.getOrderId())
                                .productId(inventory.getProductId())
                                .serialCode(inventory.getSerialCode())
                                .boxCode(inventory.getBoxCode())
                                .build()
                ));
    }

    // return: Map<orderItemId, FranchiseInventoryCommand>
    public Map<Long, FranchiseInventoryCommand> getInventoriesByOrderItemIds(List<Long> orderItemIds) {
        List<FranchiseInventory> inventories = inventoryRepository.findAllByOrderItemIdIn(orderItemIds);

        if (inventories == null || inventories.isEmpty()) {
            // 예외 처리 추가
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
                                .build()
                ));
    }
}
