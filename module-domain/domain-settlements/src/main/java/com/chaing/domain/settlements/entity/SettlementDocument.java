package com.chaing.domain.settlements.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.settlements.enums.DocumentOwner;
import com.chaing.domain.settlements.enums.DocumentType;
import com.chaing.domain.settlements.enums.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
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
@Table(name = "settlement_document", indexes = {
        @Index(name = "idx_doc_monthly", columnList = "monthly_settlement_id"),
        @Index(name = "idx_doc_daily", columnList = "daily_receipt_id"),
        @Index(name = "idx_doc_type", columnList = "doc_type")
})
public class SettlementDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementDocumentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodType periodType; // DAILY, MONTHLY

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 255)
    private DocumentType documentType; // RECEIPT_PDF, VOUCHER_EXCEL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentOwner documentOwner; // FRANCHISE, HQ

    @Column(name = "franchise_id")
    private Long franchiseId; // 가맹점별 문서 식별 강화

    @Column(name = "monthly_settlement_id")
    private Long monthlySettlementId; // 월별 문서 연결

    @Column(name = "daily_receipt_id")
    private Long dailyReceiptId; // 일별 문서 연결

    @Column(name = "settlement_date")
    private java.time.LocalDate settlementDate; // 본사 통합용

    @Column(name = "settlement_month")
    private java.time.YearMonth settlementMonth; // 본사 통합용

    @Column(nullable = false, length = 50)
    private String storageProvider; // MINIO or S3 //

    @Column(nullable = false, length = 100)
    private String bucket;

    @Column(nullable = false, length = 500)
    private String objectKey;

    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String contentType; // pdf, excel

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 100)
    private String checksum;

    @PrePersist
    private void validatePeriodFk() {
        if (periodType == null) {
            throw new IllegalStateException("periodType은 필수입니다");
        }
        if (documentType == null) {
            throw new IllegalStateException("documentType은 필수입니다");
        }
        if (documentOwner == null) {
            throw new IllegalStateException("documentOwner는 필수입니다");
        }
        // 1. 문서 타입별 소유자 및 주기 엄격 검증 (보험)
        if (documentType == DocumentType.HQ_DAILY_SUM) {
            if (documentOwner != DocumentOwner.HQ || periodType != PeriodType.DAILY) {
                throw new IllegalStateException("본사 일별 요약 리포트는 HQ 소유이며 DAILY 주기여야 합니다.");
            }
            if (settlementDate == null) {
                throw new IllegalStateException("본사 일별 요약 리포트는 settlementDate가 필수입니다.");
            }
            return;
        }

        if (documentType == DocumentType.HQ_MONTHLY_SUM) {
            if (documentOwner != DocumentOwner.HQ || periodType != PeriodType.MONTHLY) {
                throw new IllegalStateException("본사 월별 요약 리포트는 HQ 소유이며 MONTHLY 주기여야 합니다.");
            }
            if (settlementMonth == null) {
                throw new IllegalStateException("본사 월별 요약 리포트는 settlementMonth가 필수입니다.");
            }
            return;
        }

        if (documentType == DocumentType.VOUCHER_EXCEL) {
            if (documentOwner != DocumentOwner.HQ || periodType != PeriodType.MONTHLY) {
                throw new IllegalStateException("본사 월별 정산 엑셀은 HQ 소유이며 MONTHLY 주기여야 합니다.");
            }
            if (settlementMonth == null) {
                throw new IllegalStateException("본사 월별 정산 엑셀은 settlementMonth가 필수입니다.");
            }
            return;
        }

        // 2. 가맹점 문서인 경우 franchiseId 필수 체크
        if (documentOwner == DocumentOwner.FRANCHISE && (franchiseId == null || franchiseId <= 0)) {
            throw new IllegalStateException("가맹점 문서는 franchiseId가 필수이며 양수여야 합니다.");
        }

        // 가집계(PROVISIONAL) 문서는 연관 ID가 없어도 허용
        boolean isProvisional = documentType.name().startsWith("PROVISIONAL");
        if (isProvisional) {
            return;
        }

        if (periodType == PeriodType.MONTHLY && monthlySettlementId == null) {
            throw new IllegalStateException("MONTHLY 문서는 monthlySettlementId가 필수입니다");
        }
        if (periodType == PeriodType.DAILY && dailyReceiptId == null) {
            throw new IllegalStateException("DAILY 문서는 dailyReceiptId가 필수입니다");
        }
    }
}