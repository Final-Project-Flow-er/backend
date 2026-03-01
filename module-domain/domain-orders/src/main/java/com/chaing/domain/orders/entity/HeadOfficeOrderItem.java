package com.chaing.domain.orders.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_head_order_product_id",
                        columnNames = {"head_office_order_id", "product_id"}
                )
        }
)
public class HeadOfficeOrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long headOfficeOrderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_office_order_id")
    private HeadOfficeOrder headOfficeOrder;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    public void update(Long productId, Integer quantity) {
        if (productId != null &&  quantity != null) {
            this.productId = productId;
            this.quantity = quantity;
        } else {
            throw new HQOrderException(HQOrderErrorCode.INVALID_INPUT);
        }
    }
}
