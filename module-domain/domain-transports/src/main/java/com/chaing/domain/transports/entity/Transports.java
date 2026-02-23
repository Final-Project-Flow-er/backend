package com.chaing.domain.transports.entity;

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

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Transports extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transportId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String manager;                 // 운송 업체 담당자(대표)명

    @Column(nullable = false)
    private String office_phone;            // 업체 전화번호

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer ownedVehicles;         // 보유 차량 대수

    @Column(nullable = false)
    private Long unitPrice;             // 운송 단가(박스 + km 당)

    @Column(nullable = false)
    private LocalDate contractStartDate;  // 계약 시작일

    @Column(nullable = false)
    private LocalDate contractEndDate;    // 계약 종료일

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Region usableRegion;           // 주력 운송 지역

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UsableStatus status;
}
