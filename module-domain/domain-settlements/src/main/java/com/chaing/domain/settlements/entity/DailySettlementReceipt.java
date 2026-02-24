package com.chaing.domain.settlements.entity;

import com.chaing.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal adjustmentAmount;

    public BigDecimal calculatedFinal() {
        return totalSaleAmount
                .subtract(orderAmount
                        .add(deliveryFee)
                        .add(commissionFee)
                        .add(lossAmount))
                .add(refundAmount)
                .add(adjustmentAmount);
    }

    @PrePersist
    @PreUpdate
    private void validateFinalAmount() {
        BigDecimal expected = calculatedFinal();
        if (finalAmount == null || expected == null || finalAmount.compareTo(expected) != 0) {
            throw new IllegalStateException(
                    "finalAmount(" + finalAmount + ")가 계산값(" + expected + ")과 일치하지 않습니다."
            );
        }
    }

}
