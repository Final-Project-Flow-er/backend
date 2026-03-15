package com.chaing.domain.inventories.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.LogType;
import com.chaing.core.enums.ReturnItemStatus;
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
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HQInventory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    // orderId
    @NotBlank
    @Column(nullable = false)
    private Long orderId;

    // orderItemId
    @NotBlank
    @Column(nullable = false)
    private Long orderItemId;

    // 제품식별코드
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

    // 상태
    @NotNull
    @Enumerated(EnumType.STRING)
    private LogType status;

    // 박스코드
    @Column
    private String boxCode;

    @Column
    LocalDateTime shippedAt;        // 배송 완료 일자

    @Column
    LocalDateTime receivedAt;       // 입고 완료 일자

    @Column
    @Builder.Default
    Boolean isInspected = false;

    @Column
    @Enumerated(EnumType.STRING)
    ReturnItemStatus returnItemStatus;

    public void updateInspection(Boolean isInspected, ReturnItemStatus status) {
        this.isInspected = isInspected;
        this.returnItemStatus = status;
    }
}