package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseOrderItemRepository extends JpaRepository<FranchiseOrderItem, Long> {
    List<FranchiseOrderItem> findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(List<Long> orderItemIds);

    List<FranchiseOrderItem> findAllByFranchiseOrder_FranchiseIdAndFranchiseOrder_OrderCode(Long franchiseId, String orderCode);

    // 발주 ID 목록으로 OrderItem 조회
    List<FranchiseOrderItem> findAllByFranchiseOrderFranchiseOrderIdIn(List<Long> orderIds);

    List<FranchiseOrderItem> findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(List<Long> orderIds);

    List<FranchiseOrderItem> findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(Long orderId);

    List<FranchiseOrderItem> findAllByFranchiseOrder_OrderCodeInAndDeletedAtIsNull(List<String> orderCodes);

    @Query("select fo.franchiseOrder.franchiseOrderId from FranchiseOrderItem fo where fo.franchiseOrderItemId = :orderItemId")
    Long getOrderIdByOrderItemId(@Param("orderItemId") Long orderItemId);
}
