package com.chaing.domain.orders.service;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.exception.FranchiseOrderErrorCode;
import com.chaing.domain.orders.exception.FranchiseOrderException;
import com.chaing.domain.orders.repository.FranchiseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FranchiseOrderService {

    private final FranchiseOrderRepository franchiseOrderRepository;

    // 가맹점 발주 목록 조회
    public List<FranchiseOrder> getAllOrders(Long franchiseId, String username) {
        return franchiseOrderRepository.findAllByFranchiseIdAndUsername(franchiseId, username);
    }

    // 발주 번호에 따른 가맹점 특정 발주 조회
    public FranchiseOrder getOrder(Long franchiseId, String username, String orderCode) {
        return franchiseOrderRepository.findByFranchiseIdAndUsernameAndOrderCode(franchiseId, username, orderCode)
                .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));
    }
}
