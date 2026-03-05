package com.chaing.domain.settlements.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.settlements.enums.SettlementLogType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
@Table(name = "settlement_log",
        indexes = {
                    @Index(name = "idx_log_type", columnList = "type"),
                    @Index(name = "idx_log_franchise", columnList = "franchise_id")
        }
        )
public class SettlementLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementLogId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementLogType type;  // CONFIRM, DOC, ADJUSTMENT, CANCEL

    @Column(name = "franchise_id", nullable = false)
    private Long franchiseId;

    @Column(nullable = false, length = 500)
    private String content;  // "강남점 2026-02 정산 최종확정"

    @Column(name = "actor_id", nullable = false)
    private Long actorId;  // 처리자 ID

    @Column(nullable = false, length = 100)
    private String actorName;  // 처리자 이름

}
