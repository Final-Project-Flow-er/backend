package com.chaing.domain.returns.repository.interfaces;

import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;

import java.util.List;

public interface FranchiseReturnRepositoryCustom {
    List<FranchiseReturnAndReturnItemResponse> searchAllReturns(Long franchiseId);
}
