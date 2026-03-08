package com.chaing.domain.transports.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.TransportCreateCommand;
import com.chaing.domain.transports.dto.command.TransportUpdateCommand;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
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

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Transport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transportId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String manager;                 // 운송 업체 담당자(대표)명

    @Column(nullable = false)
    private String officePhone;            // 업체 전화번호

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer ownedVehicles;         // 보유 차량 대수

    @Column(nullable = false)
    private Long unitPrice;               // 운송 단가(박스 + km 당)

    @Column(nullable = false)
    private LocalDate contractStartDate;  // 계약 시작일

    @Column(nullable = false)
    private LocalDate contractEndDate;    // 계약 종료일

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Region usableRegion;           // 주력 운송 지역

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UsableStatus status = UsableStatus.ACTIVE;

    public static Transport createTransport(TransportCreateCommand command) {
        return Transport.builder()
                .companyName(command.companyName())
                .manager(command.manager())
                .officePhone(command.officePhone())
                .address(command.address())
                .ownedVehicles(command.ownedVehicles())
                .unitPrice(command.unitPrice())
                .contractStartDate(command.contractStartDate())
                .contractEndDate(command.contractEndDate())
                .usableRegion(command.usableRegion())
                .build();
    }

    public void updateTransport(TransportUpdateCommand command) {
        if (command.companyName() != null) this.companyName = command.companyName();
        if (command.manager() != null) this.manager = command.manager();
        if (command.officePhone() != null) this.officePhone = command.officePhone();
        if (command.address() != null) this.address = command.address();
        if (command.ownedVehicles() != null) this.ownedVehicles = command.ownedVehicles();
        if (command.unitPrice() != null) this.unitPrice = command.unitPrice();
        if (command.contractStartDate() != null) this.contractStartDate = command.contractStartDate();
        if (command.contractEndDate() != null) this.contractEndDate = command.contractEndDate();
        if (command.usableRegion() != null) this.usableRegion = command.usableRegion();

        if (this.contractEndDate.isBefore(this.contractStartDate)) {
            throw new TransportException(TransportErrorCode.INVALID_CONTRACT_PERIOD);
        }
    }

    public void updateStatus(UsableStatus status) {
        this.status = status;
    }
}
