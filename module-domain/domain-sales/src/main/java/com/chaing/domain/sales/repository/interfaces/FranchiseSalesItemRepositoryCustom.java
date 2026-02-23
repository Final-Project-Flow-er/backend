package com.chaing.domain.sales.repository.interfaces;

import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import com.chaing.domain.sales.entity.SalesItem;

import java.util.List;

public interface FranchiseSalesItemRepositoryCustom {
    // 판매 목록 조회
    List<FranchiseSalesInfoResponse> searchAllSalesItems(Long franchiseId);

    List<SalesItem> searchAllSalesItemsBySalesCode(Long franchiseId, String salesCode);
}
