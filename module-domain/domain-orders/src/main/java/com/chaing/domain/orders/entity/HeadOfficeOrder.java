package com.chaing.domain.orders.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.orders.enums.HQOrderStatus;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
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
public class HeadOfficeOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long headOfficeOrderId;

    @Column(nullable = false, unique = true)
    private String orderCode;

    @Column(nullable = false)
    private Long hqId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDateTime manufactureDate;

    @Column
    private String description;

    @Column
    @Builder.Default
    private String storedDate = "-";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HQOrderStatus orderStatus = HQOrderStatus.PENDING;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRegular = true;

    public void update(LocalDateTime manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public void cancel() {
        if (orderStatus == HQOrderStatus.CANCELED) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ALREADY_CANCELED);
        } else if (orderStatus != HQOrderStatus.PENDING) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_NOT_PENDING);
        }

        this.orderStatus = HQOrderStatus.CANCELED;
    }
}
