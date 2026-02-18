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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "settlement_document",
        indexes = {
                @Index(name = "idex_doc_monthly", columnList = "monthly_settlement_id"),
                @Index(name = "idex_doc_daily", columnList = "daily_receit_id"),
                @Index(name = "idex_doc_type", columnList = "document_type")
        }
)
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

    @Column(name = "daily_receip_id")
    private Long dailyReceiptId; // 일별 문서 연결

    @Column(nullable = false, length =50)
    private String storageProvider; // MINIO or S3

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

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    private LocalDateTime deletedAt;
}
