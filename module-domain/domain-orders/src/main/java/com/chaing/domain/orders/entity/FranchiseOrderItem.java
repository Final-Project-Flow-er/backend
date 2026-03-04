package com.chaing.domain.orders.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.orders.dto.info.FranchiseOrderItemInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FranchiseOrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long franchiseOrderItemId;  // pk 발주에 포함된 상품 식별 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_order_id")
    private FranchiseOrder franchiseOrder;    // fk 가맹점 발주 식별 키

    @Column(nullable = false, unique = true)
    private String serialCode;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    public void allocateFranchiseOrder(FranchiseOrder franchiseOrder) {
        this.franchiseOrder = franchiseOrder;
    }

    public void update(FranchiseOrderItemInfo item) {
        this.unitPrice = item.unitPrice();
    }
}
