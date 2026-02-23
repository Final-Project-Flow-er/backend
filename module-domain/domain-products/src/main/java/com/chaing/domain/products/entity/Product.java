package com.chaing.domain.products.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.products.enums.ProductStatus;
import com.chaing.domain.products.enums.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
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
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    // 제품코드 -> 제품코드로 오리지널 or 마라 구분 가능, 맵기 구분 가능, 인분 구분 가능
    @NotBlank
    @Column(nullable = false)
    private String productCode;

    // 이름
    @NotBlank
    @Column(nullable = false)
    private String name;

    // 설명
    @NotBlank
    @Column(nullable = false)
    private String description;

    // OR, RO, MA
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    // 이미지 URL
    @NotBlank
    @Column(nullable = false)
    private String imageUrl;

    // 소비자 판매가
    @NotNull
    @Column(nullable = false)
    private BigDecimal price;

    // 원가
    @NotNull
    @Column(nullable = false)
    private BigDecimal costPrice;

    //공급가
    @NotNull
    @Column(nullable = false)
    private BigDecimal supplyPrice;

    // 안전재고
    @NotNull
    @Column(nullable = false)
    private Integer safetyStock;

    // 판매 중, 판매예정, 임시 품절, 단종
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    // kcal
    @NotNull
    @Column(nullable = false)
    private Integer kcal;
}
