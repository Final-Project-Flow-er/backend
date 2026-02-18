package com.chaing.domain.orders.service;

import com.chaing.domain.orders.dto.command.FranchiseOrderUpdateCommand;
import com.chaing.domain.orders.dto.info.FranchiseOrderItemInfo;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.FranchiseOrderErrorCode;
import com.chaing.domain.orders.exception.FranchiseOrderException;
import com.chaing.domain.orders.repository.FranchiseOrderItemRepository;
import com.chaing.domain.orders.repository.FranchiseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FranchiseOrderService {

    private final FranchiseOrderRepository franchiseOrderRepository;
    private final FranchiseOrderItemRepository franchiseOrderItemRepository;

    // 가맹점 발주 목록 조회
    public List<FranchiseOrder> getAllOrders(Long franchiseId, String username) {
        return franchiseOrderRepository.findAllByFranchiseIdAndUsername(franchiseId, username);
    }

    // 발주 번호에 따른 가맹점 특정 발주 조회
    public FranchiseOrder getOrder(Long franchiseId, String username, String orderCode) {
        return franchiseOrderRepository.findByFranchiseIdAndUsernameAndOrderCode(franchiseId, username, orderCode)
                .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));
    }

    // 가맹점의 발주 수정
    public void updateOrder(FranchiseOrder order, FranchiseOrderUpdateCommand request) {
        // 발주 상태가 PENDING 아니면 예외 발생
        if (order.getOrderStatus() != FranchiseOrderStatus.PENDING) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_INVALID_STATUS);
        }

        // request.items를 뽑아내서 따로 수정
        for (FranchiseOrderItemInfo item : request.items()) {
            FranchiseOrderItem orderItem = franchiseOrderItemRepository.findByFranchiseOrder_FranchiseOrderIdAndProductId(order.getFranchiseOrderId(), item.productId())
                    .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));
            orderItem.update(item);
        }

        // 발주 수정
        order.update(request);
    }
}
