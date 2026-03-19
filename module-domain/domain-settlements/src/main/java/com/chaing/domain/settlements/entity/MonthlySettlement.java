package com.chaing.domain.settlements.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.settlements.enums.SettlementStatus;
import com.chaing.domain.settlements.exception.SettlementErrorCode;
import com.chaing.domain.settlements.exception.SettlementException;
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
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Getter
@Builder
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "monthly_settlement", uniqueConstraints = @UniqueConstraint(name = "uk_monthly_settlement_franchise_month", columnNames = {
        "franchise_id", "settlement_month" }), indexes = {
                @Index(name = "idx_monthly_settlement_month", columnList = "settlement_month"),
                @Index(name = "idx_monthly_settlement_franchise", columnList = "franchise_id"),
                @Index(name = "idx_monthly_settlement_status", columnList = "status")
        })

public class MonthlySettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long monthlySettlementId;

    @Column(name = "franchise_id", nullable = false)
    private Long franchiseId;

    @jakarta.persistence.Convert(converter = com.chaing.domain.settlements.converter.YearMonthConverter.class)
    @Column(name = "settlement_month", nullable = false)
    private YearMonth settlementMonth;

    // 가맹점 정산
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

    public void requestConfirm() {
        if (this.status != SettlementStatus.CALCULATED) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS);
        }
        this.status = SettlementStatus.CONFIRM_REQUESTED;
    }

    public void confirm() {
        if (this.status != SettlementStatus.CONFIRM_REQUESTED) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS);
        }
        this.status = SettlementStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void rollback() {
        if (this.status != SettlementStatus.CONFIRM_REQUESTED) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS);
        }
        this.status = SettlementStatus.CALCULATED;
    }

    public void updateAmounts(BigDecimal totalSale, BigDecimal order, BigDecimal delivery,
                              BigDecimal commission, BigDecimal loss, BigDecimal refund,
                              BigDecimal adjustment, BigDecimal finalAmt) {
        this.totalSaleAmount = totalSale;
        this.orderAmount = order;
        this.deliveryFee = delivery;
        this.commissionFee = commission;
        this.lossAmount = loss;
        this.refundAmount = refund;
        this.adjustmentAmount = adjustment;
        
        // 데이터 무결성 검증: 상세 항목의 합계와 전달받은 최종 금액이 일치하는지 확인
        BigDecimal expectedFinal = calculatedFranchisedFinal();
        if (finalAmt != null && finalAmt.compareTo(expectedFinal) != 0) {
            log.error("Settlement Total Mismatch: Expected {}, but received {} for Franchise {}", 
                expectedFinal, finalAmt, this.franchiseId);
            // 불일치 시 시스템이 계산한 정확한 값으로 강제 보정하거나 예외를 던질 수 있음
            // 여기서는 정밀한 정산을 위해 계산된 값으로 할당함
        }
        
        this.finalSettlementAmount = expectedFinal;
        this.calculatedAt = LocalDateTime.now();
    }
}
