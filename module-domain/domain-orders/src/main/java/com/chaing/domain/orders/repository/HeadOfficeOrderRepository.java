package com.chaing.domain.orders.repository;

import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.enums.HQOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeadOfficeOrderRepository extends JpaRepository<HeadOfficeOrder, Long> {
    List<HeadOfficeOrder> findAllByDeletedAtIsNull();

    Optional<HeadOfficeOrder> findByOrderCode(String orderCode);

    Optional<HeadOfficeOrder> findByHeadOfficeOrderId(Long orderId);

    List<HeadOfficeOrder> findAllByOrderStatus(HQOrderStatus hqOrderStatus);

    List<HeadOfficeOrder> findAllByOrderStatusNot(HQOrderStatus hqOrderStatus);

    List<HeadOfficeOrder> findAllByOrderCodeIn(@NotNull List<@NotBlank String> orderCodes);
}
