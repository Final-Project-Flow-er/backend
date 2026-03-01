package com.chaing.domain.businessunits.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Headquarter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hqId;

    @Column(nullable = false, unique = true)
    private String hqCode;

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

    public void updateHqInfo(BusinessUnitUpdateCommand command) {
        this.name = command.name();
        this.address = command.address();
        this.phone = command.phone();
        this.representativeName = command.representativeName();
    }
}
