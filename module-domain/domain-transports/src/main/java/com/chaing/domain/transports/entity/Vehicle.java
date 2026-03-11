package com.chaing.domain.transports.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.VehicleCreateCommand;
import com.chaing.domain.transports.dto.command.VehicleUpdateCommand;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.enums.VehicleType;
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
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

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
        if (command.transportId() != null) this.transportId = command.transportId();
        if (command.vehicleNumber() != null) this.vehicleNumber = command.vehicleNumber();
        if (command.vehicleType() != null) this.vehicleType = command.vehicleType();
        if (command.driverName() != null) this.driverName = command.driverName();
        if (command.driverPhone() != null) this.driverPhone = command.driverPhone();
        if (command.maxLoad() != null) this.maxLoad = command.maxLoad();
        if (command.dispatchable() != null) this.dispatchable = command.dispatchable();
        if (command.status() != null) {
            this.status = command.status();
            if (this.status == UsableStatus.ACTIVE) {
                if (this.dispatchable == Dispatchable.UNAVAILABLE) {
                    this.dispatchable = Dispatchable.AVAILABLE;
                }
            } else if (this.status == UsableStatus.INACTIVE) {
                this.dispatchable = Dispatchable.UNAVAILABLE;
            }
        }
    }

    public void updateStatus(UsableStatus status) {
        this.status = status;
        if (status == UsableStatus.ACTIVE) {
            this.dispatchable = Dispatchable.AVAILABLE;
        } else if (status == UsableStatus.INACTIVE) {
            this.dispatchable = Dispatchable.UNAVAILABLE;
        }
    }

    public void delete() {
        super.delete();
        this.status = UsableStatus.INACTIVE;
        this.dispatchable = Dispatchable.UNAVAILABLE;
    }
}
