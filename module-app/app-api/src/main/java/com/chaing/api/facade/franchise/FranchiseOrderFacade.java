package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.orders.response.FranchiseOrderResponse;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.service.FranchiseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseOrderFacade {

    private final FranchiseOrderService franchiseOrderService;
//    private final FranchiseService franchiseService;

    // 가맹점 발주 조회
    public List<FranchiseOrderResponse> getAllOrders(String username) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        List<FranchiseOrder> orders = franchiseOrderService.getAllOrders(franchiseId, username);

        return FranchiseOrderResponse.from(orders);
    }
}
