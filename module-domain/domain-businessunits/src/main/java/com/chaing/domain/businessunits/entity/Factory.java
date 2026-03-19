package com.chaing.domain.businessunits.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.command.BusinessUnitCreateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@Table(name = "factory", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_factory_name_deleted_at",
                columnNames = {"name", "deleted_at"}
        )
})
public class Factory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long factoryId;

    @Column(nullable = false, unique = true)
    private String factoryCode;

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

    @Builder.Default
    @Column(nullable = false)
    private int productionLineCount = 0;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UsableStatus status = UsableStatus.ACTIVE;

    public static Factory from(BusinessUnitCreateCommand command, String generatedCode) {
        var detail = command.factoryCreate();

        if (detail.productionLineCount() < 0) {
            throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_PRODUCTION_LINE_COUNT);
        }

        return Factory.builder()
                .factoryCode(generatedCode)
                .name(command.name())
                .address(command.address())
                .phone(command.phone())
                .representativeName(command.representativeName())
                .businessNumber(command.businessNumber())
                .region(command.region())
                .productionLineCount(detail.productionLineCount())
                .status(UsableStatus.ACTIVE)
                .build();
    }

    public void updateFactoryInfo(BusinessUnitUpdateCommand command) {
        if (command.name() != null) this.name = command.name();
        if (command.address() != null) this.address = command.address();
        if (command.phone() != null) this.phone = command.phone();
        if (command.representativeName() != null) this.representativeName = command.representativeName();
        if (command.region() != null) this.region = command.region();

        if (command.factoryUpdate() != null) {
            var detail = command.factoryUpdate();

            if (detail.productionLineCount() != null) {
                if (detail.productionLineCount() < 0) {
                    throw new BusinessUnitException(BusinessUnitErrorCode.INVALID_PRODUCTION_LINE_COUNT);
                }
                this.productionLineCount = detail.productionLineCount();
            }
        }
    }

    public void updateStatus(UsableStatus status) {
        this.status = status;
    }
}
