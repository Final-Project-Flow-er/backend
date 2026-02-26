package com.chaing.domain.returns.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
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

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Returns extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnId;

    @Column(nullable = false)
    private Long franchiseId;

    @Column(nullable = false)
    private Long franchiseOrderId;  // fk, FranchiseOrderId

    @Column(nullable = false, unique = true)
    private String returnCode;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReturnType returnType = ReturnType.PRODUCT_DEFECT;

    @Column
    private String description;

    @Column(nullable = false)
    private Integer totalReturnQuantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalReturnAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReturnStatus returnStatus = ReturnStatus.PENDING;

    public void cancel() {
        // 반품의 상태가 대기가 아닐 시 예외 발생
        if (!this.returnStatus.equals(ReturnStatus.PENDING)) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.CANCEL_NOT_ALLOWED);
        }

        this.returnStatus = ReturnStatus.CANCELED;
    }
}
