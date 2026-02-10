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
    private Long headOrderId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDateTime manufactureDate;

    @Column
    private String requirement;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HeadOrderStatus orderStatus = HeadOrderStatus.PENDING;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRegular = true;
}
