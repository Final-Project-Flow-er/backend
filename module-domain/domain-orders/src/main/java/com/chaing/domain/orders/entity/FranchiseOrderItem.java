package com.chaing.domain.orders.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.orders.dto.info.FranchiseOrderItemInfo;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
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

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    public void allocateFranchiseOrder(FranchiseOrder franchiseOrder) {
        this.franchiseOrder = franchiseOrder;
    }

    public void update(FranchiseOrderItemInfo item) {
        this.unitPrice = item.unitPrice();
    }

    public void updateQuantity(@NotNull Integer quantity) {
        if (quantity < 1) {
            throw new OrderException(OrderErrorCode.INVALID_QUANTITY);
        }

        this.quantity = quantity;
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
