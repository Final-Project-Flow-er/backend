package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.sales.response.FranchiseSalesResponse;
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

    public List<FranchiseSalesResponse> getAllSales(String username) {
    }
}
