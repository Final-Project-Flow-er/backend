package com.chaing.domain.businessunits.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
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

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
}
