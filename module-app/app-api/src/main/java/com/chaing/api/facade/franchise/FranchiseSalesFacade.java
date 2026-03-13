package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.sales.response.FranchiseSalesResponse;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.sales.dto.request.FranchiseSellItemRequest;
import com.chaing.domain.sales.dto.request.FranchiseSellRequest;
import com.chaing.domain.sales.dto.response.FranchiseSalesCancellationResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesDetailResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.dto.response.FranchiseSellItemResponse;
import com.chaing.domain.sales.dto.response.FranchiseSellResponse;
import com.chaing.domain.sales.service.FranchiseSalesService;
import com.chaing.domain.users.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseSalesFacade {

    private final FranchiseSalesService franchiseSalesService;
    private final UserManagementService userManagementService;
    private final FranchiseServiceImpl franchiseService;
    private final InventoryService inventoryService;

    // 미취소 판매 기록 조회
    public List<FranchiseSalesResponse> getAllSales(Long userId) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        List<FranchiseSalesInfoResponse> sales = franchiseSalesService.getAllSales(franchiseId);

        return FranchiseSalesResponse.from(sales);
    }

    // 취소 판매 기록 조회
    public List<FranchiseSalesResponse> getAllCanceledSales(Long userId) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        List<FranchiseSalesInfoResponse> canceledSales = franchiseSalesService.getAllCanceledSales(franchiseId);

        return FranchiseSalesResponse.from(canceledSales);
    }

    // 판매 기록 세부 조회
    public FranchiseSalesDetailResponse getSalesDetail(Long userId, String salesCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        return franchiseSalesService.getSalesDetail(franchiseId, salesCode);
    }

    // 판매 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseSalesCancellationResponse cancel(Long userId, String salesCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        return franchiseSalesService.cancel(franchiseId, salesCode);
    }

    // 판매 생성
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseSellResponse sell(Long userId, FranchiseSellRequest request) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // franchiseCode
        String franchiseCode = franchiseService.getById(franchiseId).code();

        FranchiseSellResponse response = franchiseSalesService.sell(franchiseId, franchiseCode, request);

        // List<serialCode>
        List<String> serialCodes = request.requestList().stream().map(FranchiseSellItemRequest::serialCode).toList();

        //재고 차감
        inventoryService.deleteFranchiseInventory(franchiseId, serialCodes);

        return response;
    }
}
