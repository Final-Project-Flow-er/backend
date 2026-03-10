package com.chaing.api.facade.inbound;

import com.chaing.api.dto.inbound.request.InboundConfirmRequest;
import com.chaing.api.dto.inbound.request.InboundScanBoxRequest;
import com.chaing.api.dto.inbound.request.InboundScanItemRequest;
import com.chaing.api.dto.inbound.response.InboundBoxSummaryResponse;
import com.chaing.api.dto.inbound.response.InboundDetailResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.dto.info.PendingBoxInfo;
import com.chaing.domain.inventories.dto.info.PendingItemInfo;
import com.chaing.domain.inventories.dto.raw.FactoryInventoryRawData;
import com.chaing.domain.inventories.dto.raw.FranchiseInventoryRawData;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.service.inbound.InboundService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.users.enums.UserRole;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Service
@Getter
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Validated
public class InboundFacade {

    private final InboundService<FranchiseInboundCreateCommand, FranchiseInventoryRawData> franchiseInboundService;
    private final InboundService<FactoryInboundCreateCommand, FactoryInventoryRawData> factoryInboundService;
    private final ProductService productService;

    @Transactional
    public void scanInboundItem(@Valid InboundScanItemRequest request) {
        factoryInboundService.scanInbound(InboundScanItemRequest.from(request));
    }

    @Transactional
    public void scanInboundBox(@Valid InboundScanBoxRequest request, Long franchiseId) {
        franchiseInboundService.scanInbound(InboundScanBoxRequest.toCommand(request, franchiseId));
    }

    public List<InboundBoxSummaryResponse> getPendingBoxes(Long franchiseId) {
        // 가맹점 내 product 조회
        List<PendingBoxInfo> boxInfos = franchiseInboundService.getBoxInfos(franchiseId);

        List<Long> productIds = boxInfos.stream()
                .map(PendingBoxInfo::productId)
                .distinct()
                .toList();

        // productId로 productName, productCode 조회
        Map<Long, ProductInfo> productMap = productService.getProductInfos(productIds);

        return boxInfos.stream()
                .map(box -> {
                    ProductInfo product = productMap.get(box.productId());
                    if (product == null) {
                        throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
                    }
                    return InboundBoxSummaryResponse.of(
                            box.boxCode(),
                            product.productName(),
                            product.productCode());
                })
                .toList();
    }

    public List<InboundDetailResponse> getPendingBoxDetails(String boxCode) {
        // boxCode로 product 조회
        List<PendingItemInfo> itemInfos = franchiseInboundService.getBoxDetails(boxCode);

        // productId로 productName, productCode 조회
        List<Long> productIds = itemInfos.stream()
                .map(PendingItemInfo::productId)
                .distinct()
                .toList();

        Map<Long, ProductInfo> productMap = productService.getProductInfos(productIds);

        return itemInfos.stream()
                .map(item -> {
                    ProductInfo product = productMap.get(item.productId());
                    return InboundDetailResponse.of(
                            item.serialCode(),
                            item.productId(),
                            product.productCode(),
                            product.productName(), // Map에서 꺼낸 상품명
                            item.manufactureDate());
                })
                .toList();
    }

    public List<InboundDetailResponse> getPendingItems() {
        // 제품 조회
        List<PendingItemInfo> itemInfos = factoryInboundService.getDetails();

        // productId로 productName, productCode 조회
        List<Long> productIds = itemInfos.stream()
                .map(PendingItemInfo::productId)
                .distinct()
                .toList();

        Map<Long, ProductInfo> productMap = productService.getProductInfos(productIds);

        return itemInfos.stream()
                .map(item -> {
                    ProductInfo product = productMap.get(item.productId());
                    return InboundDetailResponse.of(
                            item.serialCode(),
                            item.productId(),
                            product.productCode(),
                            product.productName(),
                            item.manufactureDate());
                })
                .toList();
    }

    // 입고 승인
    @Transactional
    public void confirmInbound(@Valid InboundConfirmRequest request, UserRole role) {

        List<String> selectedList = request.serialCodes();

        // 역할에 따른 입고 승인 로직
        if (role == UserRole.FACTORY) {
            factoryInboundService.confirmInbound(selectedList);
        } else if (role == UserRole.FRANCHISE) {
            franchiseInboundService.confirmInbound(selectedList);

        } else {
            throw new InventoriesException(InventoriesErrorCode.INBOUND_ROLE_INVALID);
        }
    }
}
