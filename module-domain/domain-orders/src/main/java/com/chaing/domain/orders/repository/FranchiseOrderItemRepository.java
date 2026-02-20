package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.FranchiseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FranchiseOrderItemRepository extends JpaRepository<FranchiseOrderItem, Long> {
    Optional<FranchiseOrderItem> findByFranchiseOrder_FranchiseOrderIdAndProductId(Long franchiseOrderId, Long productId);
}
