package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.FranchiseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FranchiseOrderItemRepository extends JpaRepository<FranchiseOrderItem, Long> {
    Optional<FranchiseOrderItem> findByFranchiseOrder_FranchiseOrderIdAndSerialCode(Long franchiseOrderId, String serialCode);

    List<FranchiseOrderItem> findAllByFranchiseOrderItemIdIn(List<Long> orderItemIds);

    Optional<FranchiseOrderItem> findBySerialCode(String serialCode);

    List<FranchiseOrderItem> findAllByFranchiseOrder_FranchiseIdAndFranchiseOrder_OrderCode(Long franchiseId, String orderCode);

    // 발주 ID 목록으로 OrderItem 조회
    List<FranchiseOrderItem> findAllByFranchiseOrderFranchiseOrderIdIn(List<Long> orderIds);
}
