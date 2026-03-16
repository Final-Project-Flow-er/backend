package com.chaing.domain.transports.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
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

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class TransportLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transportLogId;

    @NotBlank
    @Column(nullable = false)
    private String orderCode;

    private String returnCode;

    @NotNull
    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliverStatus deliverStatus;

    @Column(nullable = false)
    private String trackingNumber;

    // 적재량
    @NotNull
    @Column(nullable = false)
    private Long weight;

    @NotNull
    @Column(nullable = false)
    private Long franchiseId;

    public static List<TransportLog> create(List<Transit> transits) {

        if(transits == null || transits.isEmpty()){
            throw new TransportException(TransportErrorCode.TRANSPORT_NOT_FOUND);
        }

        return transits.stream()
                .map(transit -> TransportLog.builder()
                        .orderCode(transit.getOrderCode())
                        .returnCode(transit.getReturnCode())
                        .franchiseId(transit.getFranchiseId())
                        .deliverStatus(transit.getStatus())
                        .vehicleId(transit.getVehicleId())
                        .trackingNumber(transit.getTrackingNumber())
                        .weight(transit.getWeight())
                        .build())
                .toList();
    }
}


