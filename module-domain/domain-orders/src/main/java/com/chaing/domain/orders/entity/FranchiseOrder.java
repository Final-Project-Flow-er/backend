package com.chaing.domain.orders.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.orders.dto.command.FranchiseOrderCreateCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderUpdateCommand;
import com.chaing.domain.orders.dto.info.FranchiseOrderCreateInfo;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.FranchiseOrderErrorCode;
import com.chaing.domain.orders.exception.FranchiseOrderException;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
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
    private Long userId;

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
    @Builder.Default
    private List<FranchiseOrderItem> franchiseOrderItems = new ArrayList<>();

    public static FranchiseOrder create(Long franchiseId, String username, FranchiseOrderCreateCommand request, String orderCode) {
        return FranchiseOrder.builder()
                .franchiseId(franchiseId)
                .orderCode(orderCode)
                .address(request.address())
                .requirement(request.requirement())
                .deliveryDate(request.deliveryDate())
                .deliveryTime(request.deliveryTime())
                .totalAmount(BigDecimal.ZERO)
                .totalQuantity(request.items().stream().map(FranchiseOrderCreateInfo::quantity).reduce(0, Integer::sum))
                .build();
    }

    public void addOrderItem(FranchiseOrderItem item) {
        this.franchiseOrderItems.add(item);
        item.allocateFranchiseOrder(this);
    }

    public void addOrderItem(List<FranchiseOrderItem> items) {
        items.forEach(this::addOrderItem);
    }

    public void update(FranchiseOrderUpdateCommand request) {
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
            throw new OrderException(OrderErrorCode.INVALID_STATUS);
        }

        this.orderStatus = FranchiseOrderStatus.CANCELED;
    }

    public void countItems(List<FranchiseOrderItem> orderItems) {
        this.totalQuantity = orderItems.stream()
                .mapToInt(FranchiseOrderItem::getQuantity).sum();
        this.totalAmount = orderItems.stream()
                .map(FranchiseOrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void allocateTotalAmount(List<FranchiseOrderItem> orderItems) {
        this.totalAmount = orderItems.stream()
                .map(FranchiseOrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void accept() {
        if (this.orderStatus != FranchiseOrderStatus.PENDING) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_INVALID_STATUS);
        }
        this.orderStatus = FranchiseOrderStatus.ACCEPTED;
    }

    public void reject() {
        if (this.orderStatus != FranchiseOrderStatus.PENDING) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_INVALID_STATUS);
        }
        this.orderStatus = FranchiseOrderStatus.REJECTED;
    }

    // 상태를 SHIPPING_PENDING으로 수정
    public void updateStatusToShippingPending() {
        if (this.orderStatus != FranchiseOrderStatus.ACCEPTED) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_INVALID_STATUS);
        }
        this.orderStatus = FranchiseOrderStatus.SHIPPING_PENDING;
    }
}
