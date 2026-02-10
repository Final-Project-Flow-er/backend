package com.chaing.domain.sales.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_id")
    private Sales sales;

    @Column(nullable = false)
    private Long productId; // fk, 타 도메인이기 때문에 기본키만 가짐

    @Column(nullable = false)
    private String productCode;    // snapshot

    @Column(nullable = false)
    private String productName;    // snapshot

    @Column(nullable = false, unique = true)
    private String lot;             // snapshot

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;   // snapshot
}
