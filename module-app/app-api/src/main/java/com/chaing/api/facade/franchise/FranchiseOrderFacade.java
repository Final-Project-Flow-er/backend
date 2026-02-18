package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.orders.request.FranchiseOrderUpdateRequest;
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

    // 가맹점의 발주 번호에 따른 특정 발주 조회
    public FranchiseOrderResponse getOrder(String username, String orderCode) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        FranchiseOrder order = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        return FranchiseOrderResponse.from(order);
    }

    // 가맹점의 발주 수정
    @Transactional(rollbackFor = Exception.class)
    public FranchiseOrderResponse updateOrder(String username, String orderCode, FranchiseOrderUpdateRequest request) {
        // franchiseId username으로 조회하는 로직 추가 필요
        Long franchiseId = 1L;

        // 수령인이 user 목록에 없으면 예외 발생

        FranchiseOrder order = franchiseOrderService.getOrder(franchiseId, username, orderCode);

        franchiseOrderService.updateOrder(order, request.toFranchiseOrderUpdateCommand());

        return FranchiseOrderResponse.from(order);
    }
}
