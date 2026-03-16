package com.chaing.domain.orders.repository.interfaces;

import com.chaing.domain.orders.dto.response.FactoryOrderItemProjection;
import com.chaing.domain.orders.dto.response.HQOrderItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HeadOfficeOrderRepositoryCustom {
    Page<HQOrderItemProjection> findOrderItemPage(Pageable pageable);

    Page<FactoryOrderItemProjection> findFactoryOrderItemPage(boolean isAll, Pageable pageable);
}
