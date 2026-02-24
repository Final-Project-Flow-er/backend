package com.chaing.domain.settlements.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.settlements.enums.SettlementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalDateTime;
import java.time.YearMonth;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "monthly_settlement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_monthly_settlement_franchise_month",
                columnNames = {"franchise_id", "settlement_month"}
        ),
        indexes = {
                @Index(name = "idx_monthly_settlement_month", columnList = "settlement_month"),
                @Index(name = "idx_monthly_settlement_franchise", columnList = "franchise_id"),
                @Index(name = "idx_monthly_settlement_status", columnList = "status")
        }
)

public class MonthlySettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long monthlySettlementId;

    @Column(name = "franchise_id", nullable = false)
    private Long franchiseId;

    @Column(name = "settlement_month", nullable = false)
    private YearMonth settlementMonth;

    //가맹점 정산
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

    // 조정
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal adjustmentAmount;

    // 가맹점 최종 정산
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal finalSettlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementStatus status;

    private LocalDateTime calculatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime deletedAt;

    public BigDecimal calculatedFranchisedFinal() {
        return totalSaleAmount
                .subtract(orderAmount.add(deliveryFee).add(commissionFee).add(lossAmount))
                .add(refundAmount)
                .add(adjustmentAmount);
    }

    // 본사 최종 정산
    public BigDecimal calculatedHq() {
        return orderAmount.add(commissionFee)
                .add(deliveryFee)
                .subtract(refundAmount);
    }
}
