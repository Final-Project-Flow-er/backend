package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FranchiseOrderRepository extends JpaRepository<FranchiseOrder, Long> {
    List<FranchiseOrder> findAllByFranchiseIdAndUserId(Long franchiseId, Long username);

    Optional<FranchiseOrder> findByFranchiseIdAndUsernameAndOrderCode(Long franchiseId, String username, String orderCode);

    List<FranchiseOrder> findAllByFranchiseOrderIdInAndDeletedAtIsNull(List<Long> orderIds);

    Optional<FranchiseOrder> findByFranchiseIdAndFranchiseOrderId(Long franchiseId, Long orderId);

    List<FranchiseOrder> findAllByFranchiseIdAndOrderStatus(Long franchiseId, FranchiseOrderStatus orderStatus);

    List<FranchiseOrder> findAllByOrderCodeIn(List<String> orderCodes);
}
