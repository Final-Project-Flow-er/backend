package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.sales.response.FranchiseSalesResponse;
import com.chaing.domain.sales.dto.request.FranchiseSellRequest;
import com.chaing.domain.sales.dto.response.FranchiseSalesDetailResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.dto.response.FranchiseSellResponse;
import com.chaing.domain.sales.service.FranchiseSalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseSalesFacade {

    private final FranchiseSalesService franchiseSalesService;

    // 판매 기록 조회
    public List<FranchiseSalesResponse> getAllSales(String username) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        List<FranchiseSalesInfoResponse> sales = franchiseSalesService.getAllSales(franchiseId);

        return FranchiseSalesResponse.from(sales);
    }

    // 판매 기록 세부 조회
    public FranchiseSalesDetailResponse getSalesDetail(String username, String salesCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        return franchiseSalesService.getSalesDetail(franchiseId, salesCode);
    }

    // 판매 생성
    public FranchiseSellResponse sell(String username, FranchiseSellRequest request) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        FranchiseSellResponse response = franchiseSalesService.sell(franchiseId, request);

        //TODO: 재고 차감 로직 추가

        return response;
    }
}
