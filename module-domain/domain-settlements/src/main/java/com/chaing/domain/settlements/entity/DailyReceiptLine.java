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
        name = "daily_receipt_line",
        indexes = {
                @Index(name = "idx_daily_line_receipt", columnList = "daily_receipt_id"),
                @Index(name = "idx_daily_line_type", columnList = "line_type")
        }
)
public class DailyReceiptLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dailyReceiptLineId; // 영수증 상세

    @Column(name = "daily_receipt_id", nullable = false)
    private Long dailyReceiptId; // 일별 영수증 구분

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false, length = 30)
    private VoucherType lineType; // 필터

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 255)
    private String referenceCode; // 주문번호, 반품번호 등

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 255)
    private String productName;

    private Integer quantity;

    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;

}
