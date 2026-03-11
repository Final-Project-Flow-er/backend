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
        @Index(name = "idx_doc_type", columnList = "document_type")
})
public class SettlementDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementDocumentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodType periodType; // DAILY, MONTHLY

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType; // RECEIPT_PDF, VOUCHER_EXCEL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentOwner documentOwner; // FRANCHISE, HQ

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
        // HQ 소유의 전체 요약 문서는 특정 ID가 없을 수도 있지만 날짜/월은 있어야 함
        if (documentOwner == DocumentOwner.HQ && (documentType == DocumentType.HQ_DAILY_SUMMARY_PDF
                || documentType == DocumentType.HQ_MONTHLY_SUMMARY_PDF)) {
            if (documentType == DocumentType.HQ_DAILY_SUMMARY_PDF && settlementDate == null) {
                throw new IllegalStateException("HQ 일별 요약은 settlementDate가 필수입니다");
            }
            if (documentType == DocumentType.HQ_MONTHLY_SUMMARY_PDF && settlementMonth == null) {
                throw new IllegalStateException("HQ 월별 요약은 settlementMonth가 필수입니다");
            }
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