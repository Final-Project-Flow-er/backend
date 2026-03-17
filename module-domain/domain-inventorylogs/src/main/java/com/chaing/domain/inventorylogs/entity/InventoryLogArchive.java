package com.chaing.domain.inventorylogs.entity;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.enums.LocationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryLogArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @NotNull
    @Column(nullable = false)
    private Long productId;

    @NotNull
    @Column(nullable = false)
    private String productName;

    @NotNull
    @Column(nullable = false)
    private String boxCode;

    private String transactionCode;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LogType logType;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private LocationType fromLocationType;

    private Long fromLocationId;

    @Enumerated(EnumType.STRING)
    private LocationType toLocationType;

    private Long toLocationId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private ActorType actorType;

    private Long actorId;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
