package com.chaing.domain.orders.entity;

import com.chaing.domain.orders.enums.HeadOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeadOrders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long head_order_id;

    @Column(nullable = false)
    private String order_number;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String phone_number;

    @Column(nullable = false)
    private LocalDateTime manufacture_date;

    @Column
    private String requirement;

    @Column(nullable = false)
    private LocalDateTime ordered_at;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HeadOrderStatus order_status = HeadOrderStatus.PENDING;

    @Column(nullable = false)
    private Integer total_quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total_amount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRegular = true;
}
