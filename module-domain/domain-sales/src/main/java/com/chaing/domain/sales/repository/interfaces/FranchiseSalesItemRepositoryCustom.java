package com.chaing.domain.sales.repository.interfaces;

import com.chaing.domain.sales.dto.response.FranchiseSalesDailyQuantityResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.entity.SalesItem;

import java.time.LocalDate;
import java.util.List;

public interface FranchiseSalesItemRepositoryCustom {
    // 판매 목록 조회
    List<FranchiseSalesInfoResponse> searchAllSalesItems(Long franchiseId);

    List<FranchiseSalesInfoResponse> searchAllCanceledSalesItems(Long franchiseId);

    List<SalesItem> searchAllSalesItemsBySalesCode(Long franchiseId, String salesCode);

    // 안전재고 계산용: 기간 내 일자별 상품 판매수량 집계
    List<FranchiseSalesDailyQuantityResponse> searchDailyProductSalesForSafetyStock(
            List<Long> franchiseIds,
            List<Long> productIds,
            LocalDate startDate,
            LocalDate endDate
    );
}
