package com.chaing.domain.orders.repository.interfaces;

import com.chaing.domain.orders.dto.response.FranchiseOrderItemProjection;
import com.chaing.domain.orders.dto.response.HQRequestedOrderItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FranchiseOrderRepositoryCustom {
    Page<FranchiseOrderItemProjection> findOrderItemPage(Long franchiseId, Long userId, Pageable pageable);

    Page<HQRequestedOrderItemProjection> findRequestedOrderItemPage(boolean isPending, Pageable pageable);
}
