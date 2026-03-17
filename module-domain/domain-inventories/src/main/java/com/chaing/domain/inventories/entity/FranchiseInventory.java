package com.chaing.domain.inventories.entity;


import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
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
public class FranchiseInventory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    // orderId
    @NotNull
    @Column(nullable = false)
    private Long orderId;

    // orderItemId
    @NotNull
    @Column(nullable = false)
    private Long orderItemId;

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

    // 가맹점Id
    @NotNull
    @Column(nullable = false)
    private Long franchiseId;

    // 제품 상태
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LogType status;

    // 박스코드
    @NotBlank
    @Column(nullable = false)
    private String boxCode;

    // 발주코드
    private String orderCode;

    @Column
    LocalDateTime shippedAt;       // 배송 완료 일자

    @Column
    LocalDateTime receivedAt;       // 입고 완료 일자

    public static FranchiseInventory from(FranchiseInboundCreateCommand command, String serialCode, Long orderItemId) {
        return FranchiseInventory.builder()
                .boxCode(command.boxCode())
                .serialCode(serialCode)
                .productId(command.productId())
                .manufactureDate(command.manufactureDate())
                .status(LogType.INBOUND_WAIT) // 입고 스캔 시 '입고 대기' 상태
                .franchiseId(command.franchiseId())
                .orderId(command.orderId())
                .orderItemId(orderItemId)
                .build();
    }
}
