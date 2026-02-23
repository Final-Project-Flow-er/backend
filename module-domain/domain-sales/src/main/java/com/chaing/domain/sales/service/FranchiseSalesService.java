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
    private final FranchiseSalesRepository franchiseSalesRepository;

    // 판매 목록 조회
    public List<FranchiseSalesInfoResponse> getAllSales(Long franchiseId) {
        // 판매 기록 조회
        return franchiseSalesItemRepositoryCustom.searchAllSalesItems(franchiseId);
    }

    // 판매 목록 세부 조회
    public FranchiseSalesDetailResponse getSalesDetail(Long franchiseId, String salesCode) {
        // salesCode로 판매 기록 가져옴
        Sales sales = franchiseSalesRepository.findByFranchiseIdAndSalesCode(franchiseId, salesCode);

        List<SalesItem> salesItems = franchiseSalesItemRepositoryCustom.searchAllSalesItemsBySalesCode(franchiseId, salesCode);

        // 제품 정보 가져옴
        return FranchiseSalesDetailResponse.builder()
                .salesCode(sales.getSalesCode())
                .salesDate(sales.getCreatedAt())
                .products(
                        FranchiseSalesProductResponse.from(salesItems)
                )
                .build();
    }
}
