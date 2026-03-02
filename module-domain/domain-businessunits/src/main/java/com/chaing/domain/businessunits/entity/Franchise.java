package com.chaing.domain.businessunits.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
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

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Franchise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long franchiseId;

    @Column(nullable = false, unique = true)
    private String franchiseCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String representativeName;

    @Column(nullable = false, unique = true)
    private String businessNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(nullable = false)
    private String operatingDays;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    private String imageUrl;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int warningCount = 0;

    private LocalDateTime penaltyEndDate;

    @Column(nullable = false)
    private Double distanceToFactory;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UsableStatus status = UsableStatus.ACTIVE;

    public static Franchise from(BusinessUnitCreateCommand command, String generatedCode, Double distance) {
        var detail = command.franchiseCreate();

        return Franchise.builder()
                .franchiseCode(generatedCode)
                .name(command.name())
                .address(command.address())
                .phone(command.phone())
                .representativeName(command.representativeName())
                .businessNumber(command.businessNumber())
                .region(command.region())
                .operatingDays(detail.operatingDays())
                .openTime(detail.openTime())
                .closeTime(detail.closeTime())
                .imageUrl(detail.imageUrl())
                .distanceToFactory(distance)
                .status(UsableStatus.ACTIVE)
                .build();
    }

    public void updateFranchiseInfo(BusinessUnitUpdateCommand command) {
        if (command.name() != null) this.name = command.name();
        if (command.address() != null) this.address = command.address();
        if (command.phone() != null) this.phone = command.phone();
        if (command.representativeName() != null) this.representativeName = command.representativeName();
        if (command.region() != null) this.region = command.region();

        if (command.franchiseUpdate() != null) {
            var detail = command.franchiseUpdate();

            if (detail.operatingDays() != null) this.operatingDays = detail.operatingDays();
            if (detail.openTime() != null) this.openTime = detail.openTime();
            if (detail.closeTime() != null) this.closeTime = detail.closeTime();
            if (detail.imageUrl() != null) this.imageUrl = detail.imageUrl();
            if (detail.distanceToFactory() != null) this.distanceToFactory = detail.distanceToFactory();
            if (detail.penaltyEndDate() != null) this.penaltyEndDate = detail.penaltyEndDate();
        }
    }

    public void updateStatus(UsableStatus status) {
        this.status = status;
    }

    public void addWarning() {
        this.warningCount++;

        if (this.warningCount >= 3 && this.warningCount % 3 == 0) {
            applyPenalty();
        }
    }

    private void applyPenalty() {
        LocalDateTime now = LocalDateTime.now();

        if (this.penaltyEndDate != null && this.penaltyEndDate.isAfter(now)) {
            this.penaltyEndDate = this.penaltyEndDate.plusMonths(1);
        } else {
            this.penaltyEndDate = now.plusMonths(1);
        }
    }

    public boolean isReturnBlocked() {
        return this.penaltyEndDate != null && this.penaltyEndDate.isAfter(LocalDateTime.now());
    }
}
