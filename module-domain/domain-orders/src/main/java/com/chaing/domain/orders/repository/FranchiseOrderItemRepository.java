package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.FranchiseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseOrderItemRepository extends JpaRepository<FranchiseOrderItem, Long> {
    List<FranchiseOrderItem> findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(List<Long> orderItemIds);

    List<FranchiseOrderItem> findAllByFranchiseOrder_FranchiseIdAndFranchiseOrder_OrderCode(Long franchiseId, String orderCode);

    List<FranchiseOrderItem> findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(List<Long> orderIds);

    List<FranchiseOrderItem> findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(Long orderId);
}
