package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.enums.HQOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeadOfficeOrderItemRepository extends JpaRepository<HeadOfficeOrderItem, Long> {
    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderIdIn(Long hqId, List<Long> orderIds);

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(Long hqId, Long orderId);

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(Long hqId, String orderCode);

    List<HeadOfficeOrderItem> findByHeadOfficeOrder_HeadOfficeOrderIdInAndHeadOfficeOrder_OrderStatusAndDeletedAtIsNull(
            List<Long> orderIds,
            HQOrderStatus status
    );

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrder_HeadOfficeOrderIdIn(List<Long> orderIds);

    List<HeadOfficeOrderItem> findAllByHeadOfficeOrderItemIdIn(List<Long> orderItemIds);
}
