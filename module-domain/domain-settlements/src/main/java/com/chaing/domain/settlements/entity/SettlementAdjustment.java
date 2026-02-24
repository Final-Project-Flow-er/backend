package com.chaing.domain.settlements.entity;

import com.chaing.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "settlement_adjustment",
        indexes = {
                @Index(name = "idx_adjustment_voucher", columnList = "settlement_voucher_id"),
                @Index(name = "idx_adjustment_created_by", columnList = "created_by")
        }
)
public class SettlementAdjustment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementAdjustmentId;

    @Column(name = "settlement_voucher_id", nullable = false)
    private Long settlementVoucherId; // 어떤 전표 라인에 대한 조정인지

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal adjustmentAmount; // 조정 금액

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 조정을 수행한 사용자

}
