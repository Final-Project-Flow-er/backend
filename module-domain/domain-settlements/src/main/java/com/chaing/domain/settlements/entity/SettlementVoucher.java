package com.chaing.domain.settlements.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.settlements.enums.VoucherType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "settlement_voucher",
        indexes = {
                @Index(name = "idx_voucher_monthly", columnList = "monthly_settlment_id"),
                @Index(name = "idx_voucher_type", columnList = "voucher_type"),
                @Index(name = "idx_voucher_occurred_at", columnList = "occurred_at")
        }
)
public class SettlementVoucher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementVoucherId;

    @Column(name = "monthly_settlement_id", nullable = false)
    private Long monthlySettlementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_type", nullable = false, length = 30)
    private VoucherType voucherType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount; // 실제 정산 금액

    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 255)
    private String referenceCode; // 외부 연동 코드(주문번호, 발주번호)

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt; // 거래 발생 시점

    @Column(length = 255)
    private String productName;

    private Integer quantity;

    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice; // 단가

    private LocalDateTime deletedAt; // 기록용 삭제

}
