package com.chaing.domain.inventorylogs.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.enums.LocationType;
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

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    // 제품ID
    @NotNull
    @Column(nullable = false)
    private Long productId;

    // 상품 이름
    @NotNull
    @Column(nullable = false)
    private String productName;

    // 박스 코드
    private String boxCode;

    // 코드 -> 발주 코드, 판매코드, 반품 코드,,
    private String transactionCode;

    // 로그 상태 -> 입고, 출고, 반품입고 ,,,
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LogType logType;

    // 수량
    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    // 출발지 -판매같은 경우 출발지 도착지 컬럼이 없으므로 제약조건 제외
    @Enumerated(EnumType.STRING)
    private LocationType fromLocationType;

    // 해당 ID
    private Long fromLocationId;

    @Enumerated(EnumType.STRING)
    private LocationType toLocationType;

    // 해당 ID
    private Long toLocationId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    // HQ, FACTORY, FRANCHISE
    private ActorType actorType;

    private Long actorId;
}