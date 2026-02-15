package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.FranchiseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FranchiseOrderRepository extends JpaRepository<FranchiseOrder, Long> {
    List<FranchiseOrder> findAllByFranchiseIdAndUsername(Long franchiseId, String username);

    Optional<FranchiseOrder> findByFranchiseIdAndUsernameAndOrderCode(Long franchiseId, String username, String orderCode);
}
