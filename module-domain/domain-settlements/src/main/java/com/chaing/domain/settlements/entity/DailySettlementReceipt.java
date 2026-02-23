package com.chaing.domain.settlements.entity;

import com.chaing.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "daily_settlement_receipt",
        uniqueConstraints = @UniqueConstraint(
                name ="uk_daily_receipt_franchise_date",
                columnNames = {"franchise_id", "settlement_date"}
        ),
        indexes = {
                @Index(name = "idx_daily_receipt_date", columnList = "settlement_date"),
                @Index(name = "idx_daily_receipt_franchise", columnList = "franchise_id")
        }

)
public class DailySettlementReceipt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dailyReceiptId;

    @Column(name = "franchise_id", nullable = false)
    private Long franchiseId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSaleAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal orderAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal deliveryFee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionFee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lossAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal refundAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal finalAmount;

}
