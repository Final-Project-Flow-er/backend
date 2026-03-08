package com.chaing.domain.inventories.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.inventories.enums.LocationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class InventoryPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private LocationType locationType;

    //가맹점ID
    @NotNull
    @Column(nullable = false)
    private Long locationId;

    //제품ID
    @NotNull
    @Column(nullable = false)
    private Long productId;

    private Integer defaultSafetyStock;

    // 점주가 설정한 안전재고 (없으면 default 사용)
    private Integer safetyStock;
}