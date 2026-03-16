package com.chaing.domain.sales.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.sales.exception.FranchiseSalesErrorCode;
import com.chaing.domain.sales.exception.FranchiseSalesException;
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

@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sales_franchise_canceled", columnList = "franchise_id, is_canceled"),
        @Index(name = "idx_sales_franchise_canceled_created", columnList = "franchise_id, is_canceled, created_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Sales extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesId;

    @Column(nullable = false)
    private Long franchiseId;   // fk, 타 도메인이기 때문에 기본 키 값만 가짐

    @Column(nullable = false, unique = true)
    private String salesCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCanceled = false;

    public void cancel() {
        if (this.isCanceled) {
            throw new FranchiseSalesException(FranchiseSalesErrorCode.ALREADY_CANCELLED);
        }
        this.isCanceled = true;
    }
}
