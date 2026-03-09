package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.enums.HQOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeadOfficeOrderItemRepository extends JpaRepository<HeadOfficeOrderItem, Long> {
    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HeadOfficeOrderId(Long orderId);

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(String orderCode);

    List<HeadOfficeOrderItem> findByHeadOfficeOrder_HeadOfficeOrderIdInAndHeadOfficeOrder_OrderStatusAndDeletedAtIsNull(
            List<Long> orderIds,
            HQOrderStatus status
    );

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HeadOfficeOrderIdIn(List<Long> orderIds);

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrderItemIdInAndDeletedAtIsNull(List<Long> orderItemIds);

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HeadOfficeUserIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(Long userId, String orderCode);
}
