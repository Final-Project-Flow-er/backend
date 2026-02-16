package com.chaing.domain.orders.entity;

import com.chaing.core.entity.BaseEntity;
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

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Transport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long transportId;

    @Column(nullable = false)
    String companyName;

    @Column(nullable = false)
    String manager;                 // 운송 업체 담당자(대표)명

    @Column(nullable = false)
    String office_phone;            // 업체 전화번호

    @Column(nullable = false)
    String address;

    @Column(nullable = false)
    Integer owned_vehicles;         // 보유 차량 대수

    @Column(nullable = false)
    Integer unit_price;             // 운송 단가(박스 + km 당)

    @Column(nullable = false)
    LocalDate contract_start_date;  // 계약 시작일

    @Column(nullable = false)
    LocalDate contract_end_date;    // 계약 종료일
}
