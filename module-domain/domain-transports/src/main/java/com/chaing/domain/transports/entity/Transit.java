package com.chaing.domain.transports.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.transports.enums.DeliverStatus;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Transit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transitId;

    // 발주 번호
    @NotBlank
    @Column(nullable = false)
    private String orderCode;

    // 차량 정보
    @NotBlank
    @Column(nullable = false)
    private Long vehicleId;

    // 운송 상태
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliverStatus status;

    // 송장 번호
    @NotBlank
    @Column(nullable = false)
    private String trackingNumber;

    // 적재량
    @NotBlank
    @Column(nullable = false)
    private Long weight;

    public static Transit create(Long vehicleId, String orderCode, Long weight, String trackingNumber) {
        return Transit.builder()
                .orderCode(orderCode)
                .vehicleId(vehicleId)
                .status(DeliverStatus.PENDING) // 생성 시 기본값은 항상 READY!
                .trackingNumber(trackingNumber)
                .weight(weight)
                .build();
    }
}
