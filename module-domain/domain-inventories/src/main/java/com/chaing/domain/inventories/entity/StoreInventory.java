package com.chaing.domain.inventories.entity;


import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.inventories.enums.InventoryStatus;
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

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreInventory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    // 제품 식별 코드
    @NotBlank
    @Column(nullable = false)
    private String serialCode;

    // 제품ID
    @NotNull
    @Column(nullable = false)
    private Long productId;

    // 제조일자
    @NotNull
    @Column(nullable = false)
    private LocalDate manufactureDate;

    // 가맹점 코드
    @NotBlank
    @Column(nullable = false)
    private String storeCode;

    // 제품 상태
    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    // 박스코드
    @NotBlank
    @Column(nullable = false)
    private String boxCode;

}
