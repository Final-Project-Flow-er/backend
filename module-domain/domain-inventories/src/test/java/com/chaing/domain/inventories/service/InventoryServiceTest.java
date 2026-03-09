package com.chaing.domain.inventories.service;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.SafetyStockRequest;
import com.chaing.domain.inventories.entity.InventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.repository.FactoryInventoryRepository;
import com.chaing.domain.inventories.repository.FranchiseInventoryRepository;
import com.chaing.domain.inventories.repository.HQInventoryRepository;
import com.chaing.domain.inventories.repository.InventoryPolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private HQInventoryRepository hqInventoryRepository;
    @Mock
    private FranchiseInventoryRepository franchiseInventoryRepository;
    @Mock
    private FactoryInventoryRepository factoryInventoryRepository;
    @Mock
    private InventoryPolicyRepository inventoryPolicyRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("전체 재고 대분류 조회")
    void getStock() {
        // given
        List<Long> ids = List.of(1L);
        String status = "SAFE";
        given(factoryInventoryRepository.getStock(ids, status)).willReturn(Map.of());

        // when
        inventoryService.getStock(ids, status);

        // then
        verify(factoryInventoryRepository).getStock(ids, status);
    }

    @Test
    @DisplayName("전체 재고 중분류 조회")
    void getBatches() {
        // given
        Long productId = 1L;
        given(factoryInventoryRepository.getBatches(productId)).willReturn(List.of());

        // when
        inventoryService.getBatches(productId);

        // then
        verify(factoryInventoryRepository).getBatches(productId);
    }

    @Test
    @DisplayName("전체 재고 소분류 조회")
    void getItems() {
        // given
        HQInventoryItemsRequest request = mock(HQInventoryItemsRequest.class);
        given(factoryInventoryRepository.getItems(request)).willReturn(List.of());

        // when
        inventoryService.getItems(request);

        // then
        verify(factoryInventoryRepository).getItems(request);
    }

    @Test
    @DisplayName("가맹점 재고 대분류 조회")
    void getFranchiseStock() {
        // given
        Long franchiseId = 1L;
        List<Long> ids = List.of(1L);
        String status = "SAFE";
        given(franchiseInventoryRepository.getFranchiseStock(franchiseId, ids, status)).willReturn(Map.of());

        // when
        inventoryService.getFranchiseStock(franchiseId, ids, status);

        // then
        verify(franchiseInventoryRepository).getFranchiseStock(franchiseId, ids, status);
    }

    @Test
    @DisplayName("가맹점 재고 중분류 조회")
    void getFranchiseBatches() {
        // given
        Long franchiseId = 1L;
        Long productId = 1L;
        given(franchiseInventoryRepository.getFranchiseBatches(franchiseId, productId)).willReturn(List.of());

        // when
        inventoryService.getFranchiseBatches(franchiseId, productId);

        // then
        verify(franchiseInventoryRepository).getFranchiseBatches(franchiseId, productId);
    }

    @Test
    @DisplayName("가맹점 재고 소분류 조회")
    void getFranchiseItems() {
        // given
        Long franchiseId = 1L;
        FranchiseInventoryItemsRequest request = mock(FranchiseInventoryItemsRequest.class);
        given(franchiseInventoryRepository.getFranchiseItems(franchiseId, request)).willReturn(List.of());

        // when
        inventoryService.getFranchiseItems(franchiseId, request);

        // then
        verify(franchiseInventoryRepository).getFranchiseItems(franchiseId, request);
    }

    @Test
    @DisplayName("전체 가맹점 ID 조회")
    void getAllFranchiseIds() {
        // when
        inventoryService.getAllFranchiseIds();

        // then
        verify(franchiseInventoryRepository).getAllFranchiseIds();
    }

    @Test
    @DisplayName("안전재고 업데이트")
    void updateSafetyStock() {
        // given
        LocationType type = LocationType.FRANCHISE;
        Long locId = 1L;
        Long prodId = 1L;
        int stock = 10;

        InventoryPolicy existingPolicy = InventoryPolicy.builder()
                .id(100L)
                .locationType(type)
                .locationId(locId)
                .productId(prodId)
                .safetyStock(5)
                .build();

        given(inventoryPolicyRepository.findByLocationTypeAndLocationIdAndProductId(type, locId, prodId))
                .willReturn(Optional.of(existingPolicy));

        // when
        inventoryService.updateSafetyStock(type, locId, prodId, stock);

        // then
        org.mockito.ArgumentCaptor<InventoryPolicy> captor = org.mockito.ArgumentCaptor.forClass(InventoryPolicy.class);
        verify(inventoryPolicyRepository).save(captor.capture());

        InventoryPolicy savedPolicy = captor.getValue();
        assertThat(savedPolicy.getId()).isEqualTo(100L);
        assertThat(savedPolicy.getDefaultSafetyStock()).isEqualTo(10);
        assertThat(savedPolicy.getSafetyStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("수동 안전재고 설정 - 기존 정책 업데이트")
    void setSafetyStock_Update() {
        // given
        SafetyStockRequest request = new SafetyStockRequest("FRANCHISE", 1L, 1L, 30);
        when(inventoryPolicyRepository.updateManualSafetyStock(any(), any(), any(), any())).thenReturn(1L);

        // when
        inventoryService.setSafetyStock(request);

        // then
        verify(inventoryPolicyRepository).updateManualSafetyStock(LocationType.FRANCHISE, 1L, 1L, 30);
        verify(inventoryPolicyRepository, never()).save(any());
    }

    @Test
    @DisplayName("수동 안전재고 설정 - 신규 정책 생성")
    void setSafetyStock_Insert() {
        // given
        SafetyStockRequest request = new SafetyStockRequest("FRANCHISE", 1L, 1L, 30);
        when(inventoryPolicyRepository.updateManualSafetyStock(any(), any(), any(), any())).thenReturn(0L);

        // when
        inventoryService.setSafetyStock(request);

        // then
        verify(inventoryPolicyRepository).updateManualSafetyStock(LocationType.FRANCHISE, 1L, 1L, 30);
        ArgumentCaptor<InventoryPolicy> captor = ArgumentCaptor.forClass(InventoryPolicy.class);
        verify(inventoryPolicyRepository).save(captor.capture());

        InventoryPolicy savedPolicy = captor.getValue();
        assertThat(savedPolicy.getLocationType()).isEqualTo(LocationType.FRANCHISE);
        assertThat(savedPolicy.getLocationId()).isEqualTo(1L);
        assertThat(savedPolicy.getProductId()).isEqualTo(1L);
        assertThat(savedPolicy.getSafetyStock()).isEqualTo(30);
    }

    @Test
    @DisplayName("재고 부족 알림 조회 - HQ")
    void getLowStockAlerts_HQ() {
        // when
        inventoryService.getLowStockAlerts("HQ", 1L);

        // then
        verify(factoryInventoryRepository).getLowStockAlerts("HQ", null);
    }

    @Test
    @DisplayName("재고 부족 알림 조회 - 가맹점")
    void getLowStockAlerts_Franchise() {
        // when
        inventoryService.getLowStockAlerts("FRANCHISE", 2L);

        // then
        verify(franchiseInventoryRepository).getLowStockAlerts("FRANCHISE", 2L);
    }

    @Test
    @DisplayName("유통기한 알림 조회")
    void getExpirationAlerts() {
        // when
        inventoryService.getExpirationAlerts("HQ", 1L);

        // then
        verify(factoryInventoryRepository).getExpirationAlerts("HQ", 1L);
    }

    @Test
    @DisplayName("배송 상태 변경")
    void updateShippingStatus() {
        // given
        List<String> serialCodes = List.of("SERIAL1");

        // when
        inventoryService.updateShippingStatus(serialCodes);

        // then
        verify(factoryInventoryRepository).updateStatus(eq(serialCodes), eq(LogType.SHIPPING));
    }

    @Test
    @DisplayName("가맹점 배송 상태 변경")
    void updateFranchiseShippingStatus() {
        // given
        Long franchiseId = 1L;
        List<String> serialCodes = List.of("SERIAL1");

        // when
        inventoryService.updateFranchiseShippingStatus(franchiseId, serialCodes);

        // then
        verify(franchiseInventoryRepository).updateFranchiseStatus(eq(franchiseId), eq(serialCodes),
                eq(LogType.SHIPPING));
    }

    @Test
    @DisplayName("가맹점 재고 증가")
    void franchiseIncreaseInventory() {
        // given
        InventoryBatchRequest request = mock(InventoryBatchRequest.class);
        given(request.boxes()).willReturn(List.of());

        // when
        inventoryService.franchiseIncreaseInventory(request);

        // then
        verify(franchiseInventoryRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("공장 재고 증가")
    void factoryIncreaseInventory() {
        // given
        InventoryBatchRequest request = mock(InventoryBatchRequest.class);
        given(request.boxes()).willReturn(List.of());

        // when
        inventoryService.factoryIncreaseInventory(request);

        // then
        verify(factoryInventoryRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("본사 재고 증가")
    void hqIncreaseInventory() {
        // given
        InventoryBatchRequest request = mock(InventoryBatchRequest.class);
        given(request.boxes()).willReturn(List.of());

        // when
        inventoryService.hqIncreaseInventory(request);

        // then
        verify(hqInventoryRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("가맹점 재고 삭제")
    void deleteFranchiseInventory() {
        // given
        Long franchiseId = 1L;
        List<String> serialCodes = List.of("SERIAL1");

        // when
        inventoryService.deleteFranchiseInventory(franchiseId, serialCodes);

        // then
        verify(franchiseInventoryRepository).deleteFranchiseInventory(eq(franchiseId), eq(serialCodes));
    }

    @Test
    @DisplayName("공장 재고 삭제")
    void deleteFactoryInventory() {
        // when
        inventoryService.deleteFactoryInventory(List.of());

        // then
        verify(factoryInventoryRepository).deleteFactoryInventory(anyList());
    }

    @Test
    @DisplayName("본사 재고 삭제")
    void deleteHqInventory() {
        // when
        inventoryService.deleteHqInventory(List.of());

        // then
        verify(hqInventoryRepository).deleteHQInventory(anyList());
    }

}