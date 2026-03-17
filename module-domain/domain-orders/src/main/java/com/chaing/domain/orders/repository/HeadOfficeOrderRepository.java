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

    Optional<HeadOfficeOrder> findByOrderCodeAndDeletedAtIsNull(String orderCode);

    Optional<HeadOfficeOrder> findByHeadOfficeOrderIdAndDeletedAtIsNull(Long orderId);

    List<HeadOfficeOrder> findAllByOrderStatusAndDeletedAtIsNull(HQOrderStatus hqOrderStatus);

    List<HeadOfficeOrder> findAllByOrderCodeInAndDeletedAtIsNull(@NotNull List<@NotBlank String> orderCodes);

    Optional<HeadOfficeOrder> findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(Long userId, String orderCode, HQOrderStatus orderStatus);

    Optional<HeadOfficeOrder> findByUserIdAndOrderCodeAndDeletedAtIsNull(Long userId, String orderCode);
}
