package com.chaing.domain.returns.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.ReturnItemStatus;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "return_item", indexes = {
        @Index(name = "idx_ri_return_deleted", columnList = "return_id, deleted_at"),
        @Index(name = "idx_ri_box_code_deleted", columnList = "box_code, deleted_at")
})
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

    @Column(nullable = false, unique = true)
    private String boxCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReturnItemStatus returnItemStatus = ReturnItemStatus.BEFORE_INSPECTION;

    public void updateStatus(ReturnItemStatus status) {
        if (this.returnItemStatus != ReturnItemStatus.BEFORE_INSPECTION) {
            throw new FranchiseReturnException(FranchiseReturnErrorCode.INVALID_RETURN_ITEM_STATUS);
        }
        this.returnItemStatus = status;
    }
}
