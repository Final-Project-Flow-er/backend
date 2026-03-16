package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.chaing.domain.orders.repository.interfaces.FranchiseOrderRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FranchiseOrderRepository extends JpaRepository<FranchiseOrder, Long>, FranchiseOrderRepositoryCustom {
    List<FranchiseOrder> findAllByFranchiseIdAndUserId(Long franchiseId, Long username);

    Optional<FranchiseOrder> findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(Long franchiseId, Long userId, String orderCode);

    List<FranchiseOrder> findAllByFranchiseOrderIdInAndDeletedAtIsNull(List<Long> orderIds);

    Optional<FranchiseOrder> findByFranchiseIdAndFranchiseOrderId(Long franchiseId, Long orderId);

    List<FranchiseOrder> findAllByFranchiseIdAndOrderStatus(Long franchiseId, FranchiseOrderStatus orderStatus);

    List<FranchiseOrder> findAllByOrderCodeInAndDeletedAtIsNull(Set<String> orderCodes);

    List<FranchiseOrder> findAllByOrderCodeInAndDeletedAtIsNull(List<String> orderCodes);

    // 특정 가맹점 날짜 범위 발주 (ACCEPTED 일 때)
    List<FranchiseOrder> findAllByFranchiseIdAndOrderStatusAndCreatedAtBetween(
            Long franchiseId, FranchiseOrderStatus orderStatus,
            LocalDateTime start, LocalDateTime end);

    Optional<FranchiseOrder> findByFranchiseOrderIdAndDeletedAtIsNull(Long orderId);

    Optional<FranchiseOrder> findByOrderCode(String orderCode);

    Optional<FranchiseOrder> findByFranchiseIdAndUserIdAndFranchiseOrderIdAndDeletedAtIsNull(Long franchiseId, Long userId, Long orderId);

    List<FranchiseOrder> findAllByFranchiseIdAndUserIdAndOrderStatus(Long franchiseId, Long userId, FranchiseOrderStatus franchiseOrderStatus);

    List<FranchiseOrder> findAllByFranchiseIdAndUserIdAndOrderStatusNot(Long franchiseId, Long userId, FranchiseOrderStatus franchiseOrderStatus);

    List<FranchiseOrder> findAllByOrderStatusAndDeletedAtIsNull(FranchiseOrderStatus franchiseOrderStatus);

    List<FranchiseOrder> findAllByDeletedAtIsNull();

    @Query("SELECT f FROM FranchiseOrder f " +
            "WHERE f.orderStatus IN :statuses")
    List<FranchiseOrder> getFranchiseOrderByFranchiseOrderStatus(@Param("statuses") List<FranchiseOrderStatus> statuses);

    Optional<FranchiseOrder> findByOrderCodeAndDeletedAtIsNull(String orderCode);
}
