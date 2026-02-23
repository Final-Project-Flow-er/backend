package com.chaing.domain.inventories.entity;

import com.chaing.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uq_factory_inventory_policy_franchise_product",
                columnNames = {"franchise_id", "product_id"}
        )
)
public class FactoryInventoryPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //가맹점ID
    @NotNull
    @Column(name = "franchise_id", nullable = false)
    private Long franchiseId;

    //제품ID
    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // 점주가 설정한 안전재고 (없으면 default 사용)
    private Integer safetyStock;
}