package com.chaing.domain.orders.entity;

import com.chaing.domain.orders.enums.DeliverStatus;
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

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TransportLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transportLogId;

    @Column(nullable = false)
    private Long orderCode;

    @Column(nullable = false)
    private Long deliveryId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliverStatus deliverStatus = DeliverStatus.PENDING;

    @Column(nullable = false)
    private Long trackingNumber;
}
