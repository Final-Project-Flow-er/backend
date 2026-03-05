package com.chaing.domain.returns.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.enums.ReturnItemStatus;
import com.chaing.domain.returns.exception.FranchiseReturnErrorCode;
import com.chaing.domain.returns.exception.FranchiseReturnException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id")
    private Returns returns;    // fk

    @Column(nullable = false)
    private Long franchiseOrderItemId;  // fk

    @Column(nullable = false)
    @Builder.Default
    private Boolean isInspected = false;

    @Column(nullable = false)
    private String boxCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReturnItemStatus returnItemStatus = ReturnItemStatus.BEFORE_INSPECTION;

    // 반품 제품 검수 상태 업데이트
    public void update(HQReturnUpdateRequest hqReturnUpdateRequest) {
        if (hqReturnUpdateRequest == null) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_REQUEST);
        }

        this.isInspected = hqReturnUpdateRequest.isInspected();
        this.returnItemStatus = hqReturnUpdateRequest.status();
    }
}
