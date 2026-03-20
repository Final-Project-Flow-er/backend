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
@Table(name = "settlement_adjustment", indexes = {
                @Index(name = "idx_adjustment_voucher", columnList = "settlement_voucher_id"),
                @Index(name = "idx_adjustment_created_by", columnList = "created_by"),
                @Index(name = "idx_adjustment_month", columnList = "settlement_month")
})
public class SettlementAdjustment extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long settlementAdjustmentId;

        @Column(name = "settlement_voucher_id", nullable = true) // ⭐️ Null 허용 명시
        private Long settlementVoucherId; // 어떤 전표 라인에 대한 조정인지

        @Column(name = "adjustment_code", nullable = false, unique = true, length = 50)
        private String adjustmentCode; // AD-001

        @Column(name = "franchise_id", nullable = false)
        private Long franchiseId; // 가맹점 드롭다운

        @Enumerated(EnumType.STRING)
        @Column(name = "voucher_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
        private VoucherType voucherType; // 전표 유형 (기타 조정)

        @Column(name = "occurred_at", nullable = false)
        private LocalDateTime occurredAt; // 발생일

        @Column(name = "settlement_month", nullable = false, length = 7)
        private String settlementMonth; // 정산 반영월 (예: "2026-04")

        @Enumerated(EnumType.STRING)
        @Column(name = "return_type", length = 50, columnDefinition = "VARCHAR(50)")
        private com.chaing.domain.returns.enums.ReturnType returnType; // 반품 사유 (오발주, 상품하자 등)

        @Builder.Default
        @Column(name = "is_minus", nullable = false)
        private Boolean isMinus = false; // 마이너스 체크박스

        @Column(name = "created_by_name", nullable = false, length = 100)
        private String createdByName;// 등록자 이름

        @Column(nullable = false, precision = 19, scale = 2)
        private BigDecimal adjustmentAmount; // 조정 금액

        @Column(nullable = false, length = 255)
        private String reason;

        @Column(name = "created_by", nullable = false)
        private Long createdBy; // 조정을 수행한 사용자

        // IntelliJ Lombok 플러그인 버그 완화를 위한 수동 Getter
        public Boolean getIsMinus() {
                return this.isMinus;
        }

}
