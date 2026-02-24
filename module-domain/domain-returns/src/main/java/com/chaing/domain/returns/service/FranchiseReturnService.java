package com.chaing.domain.returns.service;

import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import com.chaing.domain.returns.repository.interfaces.FranchiseReturnRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FranchiseReturnService {

    private final FranchiseReturnRepository franchiseReturnRepository;
    private final FranchiseReturnRepositoryCustom franchiseReturnRepositoryCustom;

    public List<FranchiseReturnAndReturnItemResponse> getAllReturns(Long franchiseId) {
        return franchiseReturnRepositoryCustom.searchAllReturns(franchiseId);
    }
}
