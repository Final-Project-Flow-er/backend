package com.chaing.domain.sales.service;

import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.repository.interfaces.FranchiseSalesItemRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FranchiseSalesService {

    private final FranchiseSalesItemRepositoryCustom franchiseSalesItemRepositoryCustom;

    // 판매 목록 조회
    public List<FranchiseSalesInfoResponse> getAllSales(Long franchiseId) {
        // 판매 기록 조회
        return franchiseSalesItemRepositoryCustom.searchAllSalesItems(franchiseId);
    }
}
