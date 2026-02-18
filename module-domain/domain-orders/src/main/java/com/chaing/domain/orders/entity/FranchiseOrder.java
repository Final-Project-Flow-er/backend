package com.chaing.domain.orders.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.orders.dto.command.FranchiseOrderUpdateCommand;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.FranchiseOrderErrorCode;
import com.chaing.domain.orders.exception.FranchiseOrderException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FranchiseOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long franchiseOrderId;  // pk 가맹점 발주 코드

    @Column(nullable = false)
    private Long franchiseId;  // fk 가맹점 번호

    @Column(nullable = false, unique = true)
    private String orderCode;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column
    private String requirement;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FranchiseOrderStatus orderStatus = FranchiseOrderStatus.PENDING;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime deliveryDate;

    @Column(nullable = false)
    private String deliveryTime;

    @OneToMany(mappedBy = "franchiseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FranchiseOrderItem> franchiseOrderItems = new ArrayList<>();

    public void addOrderItem(FranchiseOrderItem item) {
        this.franchiseOrderItems.add(item);
        item.allocateFranchiseOrder(this);
    }

    public void update(FranchiseOrderUpdateCommand request) {
        this.username = request.username();
        this.phoneNumber = request.phoneNumber();
        this.address = request.address();
        this.requirement = request.requirement();
        this.deliveryTime = request.deliveryTime();

        this.totalQuantity = franchiseOrderItems.stream()
                .mapToInt(FranchiseOrderItem::getQuantity)
                .sum();

        this.totalAmount = franchiseOrderItems.stream()
                .map(FranchiseOrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cancel() {
        if (this.orderStatus != FranchiseOrderStatus.PENDING) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_INVALID_STATUS);
        }

        this.orderStatus = FranchiseOrderStatus.CANCELED;
    }
}
