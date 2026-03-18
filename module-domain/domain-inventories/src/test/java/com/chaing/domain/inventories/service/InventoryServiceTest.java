package com.chaing.domain.inventories.service;

import com.chaing.core.dto.command.FranchiseOrderCodeAndQuantityCommand;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.ReturnItemStatus;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.info.InboundProductIdInfo;
import com.chaing.domain.inventories.dto.info.StockInfoForLog;
import com.chaing.domain.inventories.dto.request.DisposalRequest;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.InventoryRequest;
import com.chaing.domain.inventories.dto.request.SafetyStockRequest;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.entity.HQInventory;
import com.chaing.domain.inventories.entity.InventoryPolicy;
import com.chaing.domain.inventories.enums.LocationType;
import com.chaing.domain.inventories.exception.InventoriesException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        Long productId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        given(factoryInventoryRepository.getBatches(productId, pageable))
                .willReturn(Page.empty(pageable));

        inventoryService.getBatches(productId, pageable);

        verify(factoryInventoryRepository).getBatches(productId, pageable);
    }

    @Test
    @DisplayName("전체 재고 소분류 조회")
    void getItems() {
        HQInventoryItemsRequest request = mock(HQInventoryItemsRequest.class);
        Pageable pageable = PageRequest.of(0, 10);

        given(factoryInventoryRepository.getItems(request, pageable))
                .willReturn(Page.empty(pageable));

        inventoryService.getItems(request, pageable);

        verify(factoryInventoryRepository).getItems(request, pageable);
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
        Long franchiseId = 1L;
        Long productId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        given(franchiseInventoryRepository.getFranchiseBatches(franchiseId, productId, pageable))
                .willReturn(Page.empty(pageable));

        inventoryService.getFranchiseBatches(franchiseId, productId, pageable);

        verify(franchiseInventoryRepository).getFranchiseBatches(franchiseId, productId, pageable);
    }

    @Test
    @DisplayName("가맹점 재고 소분류 조회")
    void getFranchiseItems() {
        Long franchiseId = 1L;
        FranchiseInventoryItemsRequest request = mock(FranchiseInventoryItemsRequest.class);
        Pageable pageable = PageRequest.of(0, 10);

        given(franchiseInventoryRepository.getFranchiseItems(franchiseId, request, pageable))
                .willReturn(Page.empty(pageable));

        inventoryService.getFranchiseItems(franchiseId, request, pageable);

        verify(franchiseInventoryRepository).getFranchiseItems(franchiseId, request, pageable);
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

        given(inventoryPolicyRepository.findPolicy(type, locId, prodId))
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
        verify(factoryInventoryRepository).getLowStockAlerts("HQ", 1L);
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
        inventoryService.updateShippingStatus(serialCodes, LogType.SHIPPING);

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

    @Test
    @DisplayName("기본 안전재고 초기화")
    void resetSafetyStockToDefault() {
        InventoryPolicy policy = InventoryPolicy.builder()
                .locationType(LocationType.FRANCHISE)
                .locationId(1L)
                .productId(1L)
                .defaultSafetyStock(10)
                .safetyStock(30)
                .build();

        given(inventoryPolicyRepository.findPolicy(LocationType.FRANCHISE, 1L, 1L))
                .willReturn(Optional.of(policy));

        inventoryService.resetSafetyStockToDefault("FRANCHISE", 1L, 1L);

        ArgumentCaptor<InventoryPolicy> captor = ArgumentCaptor.forClass(InventoryPolicy.class);
        verify(inventoryPolicyRepository).save(captor.capture());
        assertThat(captor.getValue().getSafetyStock()).isNull();
    }

    @Test
    @DisplayName("기본 안전재고 초기화 실패")
    void resetSafetyStockToDefault_Fail() {
        given(inventoryPolicyRepository.findPolicy(LocationType.FRANCHISE, 1L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.resetSafetyStockToDefault("FRANCHISE", 1L, 1L))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("위치 타입 변환")
    void convertLocationType() {
        assertThat(inventoryService.convertLocationType("FRANCHISE")).isEqualTo(LocationType.FRANCHISE);
    }

    @Test
    @DisplayName("위치 타입 변환 실패")
    void convertLocationType_Fail() {
        assertThatThrownBy(() -> inventoryService.convertLocationType("NOPE"))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("유통기한 알림 가맹점")
    void getExpirationAlerts_Franchise() {
        inventoryService.getExpirationAlerts("FRANCHISE", 1L);

        verify(franchiseInventoryRepository).getExpirationAlerts("FRANCHISE", 1L);
    }

    @Test
    @DisplayName("만료 상태 변경")
    void updateExpiredStatus() {
        inventoryService.updateExpiredStatus();

        verify(hqInventoryRepository).updateExpiredStatus(any(LocalDate.class));
        verify(factoryInventoryRepository).updateExpiredStatus(any(LocalDate.class));
        verify(franchiseInventoryRepository).updateExpiredStatus(any(LocalDate.class));
    }

    @Test
    @DisplayName("공장 주문 재고 조회")
    void getFactoryInventoriesByOrderId() {
        List<FactoryInventory> inventories = List.of(factoryInventory(1L, 10L, "F-1", "BOX-1", 100L));
        given(factoryInventoryRepository.findAllByOrderId(100L)).willReturn(inventories);

        assertThat(inventoryService.getFactoryInventoriesByOrderId(100L)).isEqualTo(inventories);
    }

    @Test
    @DisplayName("가맹점 주문 재고 조회")
    void getFranchiseInventoriesByOrderId() {
        List<FranchiseInventory> inventories = List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L));
        given(franchiseInventoryRepository.findAllByOrderId(100L)).willReturn(inventories);

        assertThat(inventoryService.getFranchiseInventoriesByOrderId(100L)).isEqualTo(inventories);
    }

    @Test
    @DisplayName("가맹점 박스 조회")
    void getBoxCodeFromFranchise() {
        given(franchiseInventoryRepository.findAllBySerialCodeInAndDeletedAtIsNull(List.of("S-1")))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));

        assertThat(inventoryService.getBoxCodeFromFranchise(List.of("S-1")))
                .containsEntry("S-1", "BOX-1");
    }

    @Test
    @DisplayName("가맹점 박스 조회 실패")
    void getBoxCodeFromFranchise_Fail() {
        given(franchiseInventoryRepository.findAllBySerialCodeInAndDeletedAtIsNull(List.of("S-1")))
                .willReturn(List.of());

        assertThatThrownBy(() -> inventoryService.getBoxCodeFromFranchise(List.of("S-1")))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("가맹점 상품 조회")
    void getProductIdBySerialCodeFromFranchise() {
        given(franchiseInventoryRepository.findAllBySerialCodeInAndDeletedAtIsNull(List.of("S-1")))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));

        assertThat(inventoryService.getProductIdBySerialCodeFromFranchise(List.of("S-1")))
                .containsEntry("S-1", 10L);
    }

    @Test
    @DisplayName("가맹점 주문상품 조회")
    void getSerialCodesByOrderItemIdsFromFranchise() {
        given(franchiseInventoryRepository.findAllByOrderItemIdInAndDeletedAtIsNull(List.of(11L)))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));

        assertThat(inventoryService.getSerialCodesByOrderItemIdsFromFranchise(List.of(11L)))
                .containsEntry("S-1", 11L);
    }

    @Test
    @DisplayName("박스 재고 조회")
    void getInventoriesByBoxCode() {
        given(franchiseInventoryRepository.findAllByBoxCodeIn(List.of("BOX-1")))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));

        assertThat(inventoryService.getInventoriesByBoxCode(List.of("BOX-1")))
                .containsKey("BOX-1");
    }

    @Test
    @DisplayName("주문 재고 조회")
    void getInventoriesByOrderItemIds() {
        given(franchiseInventoryRepository.findAllByOrderItemIdInAndDeletedAtIsNull(List.of(11L)))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));

        assertThat(inventoryService.getInventoriesByOrderItemIds(List.of(11L)))
                .containsKey(11L);
    }

    @Test
    @DisplayName("폐기 본사")
    void disposalInventory_HQ() {
        inventoryService.disposalInventory(new DisposalRequest("HQ", null, List.of(1L, 2L)));

        verify(hqInventoryRepository).deleteByInventoryIdIn(List.of(1L, 2L));
    }

    @Test
    @DisplayName("폐기 가맹점")
    void disposalInventory_Franchise() {
        inventoryService.disposalInventory(new DisposalRequest("FRANCHISE", 1L, List.of(1L, 2L)));

        verify(franchiseInventoryRepository).deleteByFranchiseIdAndInventoryIdIn(1L, List.of(1L, 2L));
    }

    @Test
    @DisplayName("폐기 실패")
    void disposalInventory_Fail() {
        assertThatThrownBy(() -> inventoryService.disposalInventory(new DisposalRequest("NOPE", 1L, List.of(1L))))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("수동 안전재고 타입 실패")
    void setSafetyStock_TypeFail() {
        assertThatThrownBy(() -> inventoryService.setSafetyStock(new SafetyStockRequest("BAD", 1L, 1L, 10)))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("수동 안전재고 상품 실패")
    void setSafetyStock_ProductFail() {
        assertThatThrownBy(() -> inventoryService.setSafetyStock(new SafetyStockRequest("HQ", null, null, 10)))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("누락 검증")
    void verifyOmission() {
        given(hqInventoryRepository.findAllByBoxCodeInAndDeletedAtIsNull(List.of("BOX-1")))
                .willReturn(List.of(hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1")));

        inventoryService.verifyOmission(List.of("BOX-1"));
    }

    @Test
    @DisplayName("누락 검증 실패")
    void verifyOmission_Fail() {
        given(hqInventoryRepository.findAllByBoxCodeInAndDeletedAtIsNull(List.of("BOX-1")))
                .willReturn(List.of());

        assertThatThrownBy(() -> inventoryService.verifyOmission(List.of("BOX-1")))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("검수 결과 저장")
    void saveInspectionResults() {
        HQInventory inventory = hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1");
        given(hqInventoryRepository.findAllByBoxCodeInAndDeletedAtIsNull(List.of("BOX-1")))
                .willReturn(List.of(inventory));

        inventoryService.saveInspectionResults(
                List.of("BOX-1"),
                Map.of("BOX-1", ReturnItemStatus.NORMAL),
                Map.of("S-1", true)
        );

        verify(hqInventoryRepository).saveAll(List.of(inventory));
        assertThat(inventory.getReturnItemStatus()).isEqualTo(ReturnItemStatus.NORMAL);
    }

    @Test
    @DisplayName("검수 결과 저장 실패")
    void saveInspectionResults_Fail() {
        HQInventory inventory = hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1");
        given(hqInventoryRepository.findAllByBoxCodeInAndDeletedAtIsNull(List.of("BOX-1")))
                .willReturn(List.of(inventory));

        assertThatThrownBy(() -> inventoryService.saveInspectionResults(
                List.of("BOX-1"),
                Map.of("BOX-1", ReturnItemStatus.NORMAL),
                Map.of()
        )).isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("본사 재고 아이디 조회")
    void getHqInventoriesByIds() {
        List<HQInventory> inventories = List.of(hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1"));
        given(hqInventoryRepository.findByInventoryIdIn(List.of(1L))).willReturn(inventories);

        assertThat(inventoryService.getHqInventoriesByIds(List.of(1L))).isEqualTo(inventories);
    }

    @Test
    @DisplayName("가맹점 재고 아이디 조회")
    void getFranchiseInventoriesByIds() {
        List<FranchiseInventory> inventories = List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L));
        given(franchiseInventoryRepository.findByInventoryIdIn(List.of(1L))).willReturn(inventories);

        assertThat(inventoryService.getFranchiseInventoriesByIds(List.of(1L))).isEqualTo(inventories);
    }

    @Test
    @DisplayName("공장 재고 아이디 조회")
    void getFactoryInventoriesByIds() {
        List<FactoryInventory> inventories = List.of(factoryInventory(1L, 10L, "F-1", "BOX-1", 100L));
        given(factoryInventoryRepository.findByInventoryIdIn(List.of(1L))).willReturn(inventories);

        assertThat(inventoryService.getFactoryInventoriesByIds(List.of(1L))).isEqualTo(inventories);
    }

    @Test
    @DisplayName("박스 확장 본사")
    void expandInventoryIdsByBoxCode_HQ() {
        given(hqInventoryRepository.findByInventoryIdIn(List.of(1L)))
                .willReturn(List.of(hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1")));
        given(hqInventoryRepository.findByBoxCodeIn(List.of("BOX-1")))
                .willReturn(List.of(
                        hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1"),
                        hqInventory(2L, 100L, 12L, "S-2", 10L, "BOX-1")
                ));

        assertThat(inventoryService.expandInventoryIdsByBoxCode("HQ", List.of(1L), null, null))
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("박스 확장 공장")
    void expandInventoryIdsByBoxCode_Factory() {
        given(factoryInventoryRepository.findByInventoryIdIn(List.of(1L)))
                .willReturn(List.of(factoryInventory(1L, 10L, "F-1", "BOX-1", 100L)));
        given(factoryInventoryRepository.findByBoxCodeIn(List.of("BOX-1")))
                .willReturn(List.of(
                        factoryInventory(1L, 10L, "F-1", "BOX-1", 100L),
                        factoryInventory(2L, 10L, "F-2", "BOX-1", 100L)
                ));

        assertThat(inventoryService.expandInventoryIdsByBoxCode("FACTORY", List.of(1L), null, null))
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("박스 확장 가맹점")
    void expandInventoryIdsByBoxCode_Franchise() {
        given(franchiseInventoryRepository.findByInventoryIdIn(List.of(1L)))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));
        given(franchiseInventoryRepository.findByBoxCodeInAndFranchiseId(List.of("BOX-1"), 1L))
                .willReturn(List.of(
                        franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L),
                        franchiseInventory(2L, 100L, 12L, "S-2", 10L, "BOX-1", 1L)
                ));

        assertThat(inventoryService.expandInventoryIdsByBoxCode("FRANCHISE", List.of(1L), null, 1L))
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("폐기 아이디 본사")
    void disposalInventoryByIds_HQ() {
        inventoryService.disposalInventoryByIds("HQ", List.of(1L, 2L), null, null);

        verify(hqInventoryRepository).deleteByInventoryIdIn(List.of(1L, 2L));
    }

    @Test
    @DisplayName("폐기 아이디 가맹점")
    void disposalInventoryByIds_Franchise() {
        given(franchiseInventoryRepository.findByInventoryIdInAndFranchiseId(List.of(1L, 2L), 1L))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));

        inventoryService.disposalInventoryByIds("FRANCHISE", List.of(1L, 2L), null, 1L);

        verify(franchiseInventoryRepository).deleteByFranchiseIdAndInventoryIdIn(1L, List.of(1L));
    }

    @Test
    @DisplayName("재고 확인")
    void checkStock() {
        List<FranchiseOrderCodeAndQuantityCommand> items = List.of(
                FranchiseOrderCodeAndQuantityCommand.builder().productCode("P-1").quantity(2).build()
        );
        Map<String, ProductInfo> productInfoByProductCode = Map.of(
                "P-1", ProductInfo.builder().productId(10L).build()
        );
        given(factoryInventoryRepository.findAllByProductIdInAndStatusAndDeletedAtIsNull(eq(java.util.Set.of(10L)), eq(LogType.AVAILABLE)))
                .willReturn(List.of(
                        factoryInventory(1L, 10L, "F-1", "BOX-1", 100L),
                        factoryInventory(2L, 10L, "F-2", "BOX-1", 100L)
                ));

        inventoryService.checkStock(items, productInfoByProductCode);
    }

    @Test
    @DisplayName("재고 확인 실패")
    void checkStock_Fail() {
        List<FranchiseOrderCodeAndQuantityCommand> items = List.of(
                FranchiseOrderCodeAndQuantityCommand.builder().productCode("P-1").quantity(1).build()
        );

        assertThatThrownBy(() -> inventoryService.checkStock(items, Map.of()))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("시리얼 조회")
    void getItemBySerialCode() {
        FranchiseInventory inventory1 = franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L);
        FranchiseInventory inventory2 = franchiseInventory(2L, 100L, 12L, "S-2", 10L, "BOX-1", 1L);
        given(franchiseInventoryRepository.getAllByStatusAndSerialCodeAndFranchiseId(List.of("S-1", "S-2"), 1L, LogType.AVAILABLE))
                .willReturn(List.of(inventory1, inventory2));

        List<InventoryRequest> result = inventoryService.getItemBySerialCode(List.of("S-1", "S-2"), 1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).serialCode()).isEqualTo("S-1");
    }

    @Test
    @DisplayName("시리얼 조회 실패")
    void getItemBySerialCode_Fail() {
        given(franchiseInventoryRepository.getAllByStatusAndSerialCodeAndFranchiseId(List.of("S-1"), 1L, LogType.AVAILABLE))
                .willReturn(List.of());

        assertThatThrownBy(() -> inventoryService.getItemBySerialCode(List.of("S-1"), 1L))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("주문 정보 조회")
    void getOrderInfo() {
        given(factoryInventoryRepository.getOrderIdBySerialCodeIn(List.of("S-1")))
                .willReturn(List.of(100L));

        assertThat(inventoryService.getOrderInfo(List.of("S-1"))).containsExactly(100L);
    }

    @Test
    @DisplayName("주문 정보 조회 실패")
    void getOrderInfo_Fail() {
        given(factoryInventoryRepository.getOrderIdBySerialCodeIn(List.of("S-1")))
                .willReturn(List.of());

        assertThatThrownBy(() -> inventoryService.getOrderInfo(List.of("S-1")))
                .isInstanceOf(InventoriesException.class);
    }

    @Test
    @DisplayName("본사 주문상품 조회")
    void getSerialCodesByOrderItemIdsFromHQ() {
        given(hqInventoryRepository.findAllByOrderItemIdInAndDeletedAtIsNull(List.of(11L)))
                .willReturn(List.of(hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1")));

        assertThat(inventoryService.getSerialCodesByOrderItemIdsFromHQ(List.of(11L)))
                .containsEntry("S-1", 11L);
    }

    @Test
    @DisplayName("본사 박스 조회")
    void getBoxCodeFromHQ() {
        given(hqInventoryRepository.findAllBySerialCodeInAndDeletedAtIsNull(List.of("S-1")))
                .willReturn(List.of(hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1")));

        assertThat(inventoryService.getBoxCodeFromHQ(List.of("S-1")))
                .containsEntry("S-1", "BOX-1");
    }

    @Test
    @DisplayName("본사 상품 조회")
    void getProductIdBySerialCodeFromHQ() {
        given(hqInventoryRepository.findAllBySerialCodeInAndDeletedAtIsNull(List.of("S-1")))
                .willReturn(List.of(hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1")));

        assertThat(inventoryService.getProductIdBySerialCodeFromHQ(List.of("S-1")))
                .containsEntry("S-1", 10L);
    }

    @Test
    @DisplayName("반품 검수 조회")
    void getReturnItemInspection() {
        HQInventory inventory = hqInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1");
        inventory.updateInspection(true, ReturnItemStatus.NORMAL);
        given(hqInventoryRepository.findAllByOrderItemIdInAndDeletedAtIsNull(List.of(11L)))
                .willReturn(List.of(inventory));

        assertThat(inventoryService.getReturnItemInspection(Map.of(1L, 11L)))
                .containsKey("BOX-1");
    }

    @Test
    @DisplayName("가맹점 반품 검수 조회")
    void getReturnItemInspectionFromFranchise() {
        given(franchiseInventoryRepository.findAllByOrderItemIdInAndDeletedAtIsNull(List.of(11L)))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));

        assertThat(inventoryService.getReturnItemInspectionFromFranchise(List.of(11L)))
                .containsKey("BOX-1");
    }

    @Test
    @DisplayName("로그 재고 조회 가맹점")
    void getStockBySerialCodeFromFranchise() {
        given(franchiseInventoryRepository.getAllByFranchiseIdAndSerialCodeIn(1L, List.of("S-1")))
                .willReturn(List.of(franchiseInventory(1L, 100L, 11L, "S-1", 10L, "BOX-1", 1L)));

        List<StockInfoForLog> result = inventoryService.getStockBySerialCodeFromFranchise(List.of("S-1"), 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).boxCode()).isEqualTo("BOX-1");
    }

    @Test
    @DisplayName("로그 재고 조회 공장")
    void getStockBySerialCodeFromFactory() {
        given(factoryInventoryRepository.findAllBySerialCodeIn(List.of("F-1")))
                .willReturn(List.of(factoryInventory(1L, 10L, "F-1", "BOX-1", 100L)));

        List<StockInfoForLog> result = inventoryService.getStockBySerialCodeFromFactory(List.of("F-1"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).productId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("수량 조회 공장")
    void getFactoryQuantityByBoxCodes() {
        given(factoryInventoryRepository.countByBoxCodes(List.of("BOX-1")))
                .willReturn(Map.of("BOX-1", 2L));

        assertThat(inventoryService.getFactoryQuantityByBoxCodes(List.of("BOX-1")))
                .containsEntry("BOX-1", 2L);
    }

    @Test
    @DisplayName("수량 조회 가맹점")
    void getFranchiseQuantityByBoxCodes() {
        given(franchiseInventoryRepository.countByBoxCodes(1L, List.of("BOX-1")))
                .willReturn(Map.of("BOX-1", 2L));

        assertThat(inventoryService.getFranchiseQuantityByBoxCodes(1L, List.of("BOX-1")))
                .containsEntry("BOX-1", 2L);
    }

    @Test
    @DisplayName("입고 상품 조회")
    void getProductIdFromFactory() {
        List<InboundProductIdInfo> infos = List.of(new InboundProductIdInfo(10L, 2L));
        given(factoryInventoryRepository.getInboundProductIdInfosBySerialCodes(List.of("F-1")))
                .willReturn(infos);

        assertThat(inventoryService.getProductIdFromFactory(List.of("F-1"))).isEqualTo(infos);
    }

    private FranchiseInventory franchiseInventory(Long inventoryId, Long orderId, Long orderItemId, String serialCode,
                                                  Long productId, String boxCode, Long franchiseId) {
        return FranchiseInventory.builder()
                .inventoryId(inventoryId)
                .orderId(orderId)
                .orderItemId(orderItemId)
                .serialCode(serialCode)
                .productId(productId)
                .manufactureDate(LocalDate.of(2024, 1, 1))
                .franchiseId(franchiseId)
                .status(LogType.AVAILABLE)
                .boxCode(boxCode)
                .build();
    }

    private FactoryInventory factoryInventory(Long inventoryId, Long productId, String serialCode, String boxCode, Long orderId) {
        return FactoryInventory.builder()
                .inventoryId(inventoryId)
                .orderId(orderId)
                .serialCode(serialCode)
                .productId(productId)
                .manufactureDate(LocalDate.of(2024, 1, 1))
                .status(LogType.AVAILABLE)
                .boxCode(boxCode)
                .build();
    }

    private HQInventory hqInventory(Long inventoryId, Long orderId, Long orderItemId, String serialCode,
                                    Long productId, String boxCode) {
        return HQInventory.builder()
                .inventoryId(inventoryId)
                .orderId(orderId)
                .orderItemId(orderItemId)
                .serialCode(serialCode)
                .productId(productId)
                .manufactureDate(LocalDate.of(2024, 1, 1))
                .status(LogType.AVAILABLE)
                .boxCode(boxCode)
                .build();
    }

}
