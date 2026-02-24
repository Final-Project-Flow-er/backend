package com.chaing.domain.returns.service;

import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnInfo;
import com.chaing.domain.returns.entity.ReturnItem;
import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import com.chaing.domain.returns.repository.FranchiseReturnItemRepository;
import com.chaing.domain.returns.repository.FranchiseReturnRepository;
import com.chaing.domain.returns.repository.interfaces.FranchiseReturnRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FranchiseReturnService {

    private final FranchiseReturnRepository franchiseReturnRepository;
    private final FranchiseReturnItemRepository franchiseReturnItemRepository;
    private final FranchiseReturnRepositoryCustom franchiseReturnRepositoryCustom;

    public List<FranchiseReturnAndReturnItemResponse> getAllReturns(Long franchiseId) {
        return franchiseReturnRepositoryCustom.searchAllReturns(franchiseId);
    }

    public FranchiseReturnInfo getReturn(String username, Long franchiseId, String returnCode) {
        Returns returns = franchiseReturnRepository.findByFranchiseIdAndUsernameAndReturnCode(franchiseId, username, returnCode)
                .orElseThrow(() -> new FranchiseReturnException(FranchiseReturnErrorCode.RETURN_NOT_FOUND));

        return FranchiseReturnInfo.from(returns);
    }

    // returnCode로 orderItemId 반환
    public List<Long> getAllReturnItemOrderItemId(String returnCode) {
        return franchiseReturnItemRepository.findAllByReturns_ReturnCode(returnCode)
                .stream()
                .map(ReturnItem::getFranchiseOrderItemId)
                .toList();
    }
}
