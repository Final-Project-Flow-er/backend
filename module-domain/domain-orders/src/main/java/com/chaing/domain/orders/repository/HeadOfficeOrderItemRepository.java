package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.enums.HQOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeadOfficeOrderItemRepository extends JpaRepository<HeadOfficeOrderItem, Long> {
    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HeadOfficeOrderIdAndDeletedAtIsNull(Long orderId);

    List<HeadOfficeOrderItem> findByHeadOfficeOrder_HeadOfficeOrderIdInAndHeadOfficeOrder_OrderStatusAndDeletedAtIsNull(
            List<Long> orderIds,
            HQOrderStatus status
    );

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrderItemIdInAndDeletedAtIsNull(List<Long> orderItemIds);

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_UserIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(Long userId, String orderCode);

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HeadOfficeOrderIdInAndDeletedAtIsNull(List<Long> orderIds);
}
