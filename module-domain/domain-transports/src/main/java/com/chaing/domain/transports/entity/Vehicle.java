package com.chaing.domain.transports.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.VehicleCreateCommand;
import com.chaing.domain.transports.dto.command.VehicleUpdateCommand;
import com.chaing.domain.transports.enums.Dispatchable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @Column(nullable = false)
    private Long transportId;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false)
    private String driverName;

    @Column(nullable = false)
    private String driverPhone;

    @Column(nullable = false)
    private Long maxLoad;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Dispatchable dispatchable = Dispatchable.AVAILABLE;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UsableStatus status = UsableStatus.ACTIVE;

    public static Vehicle createVehicle(VehicleCreateCommand command) {
        return Vehicle.builder()
                .transportId(command.transportId())
                .vehicleNumber(command.vehicleNumber())
                .vehicleType(command.vehicleType())
                .driverName(command.driverName())
                .driverPhone(command.driverPhone())
                .maxLoad(command.maxLoad())
                .build();
    }

    public void updateVehicle(VehicleUpdateCommand command) {
        this.transportId = command.transportId();
        this.vehicleNumber = command.vehicleNumber();
        this.vehicleType = command.vehicleType();
        this.driverName = command.driverName();
        this.driverPhone = command.driverPhone();
        this.maxLoad = command.maxLoad();
        this.dispatchable = command.dispatchable();
        this.status = command.usableStatus();
    }

    public void updateStatus(UsableStatus status) {
        this.status = status;
    }
}
