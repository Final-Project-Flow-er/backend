package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.sales.response.FranchiseSalesResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
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
}
