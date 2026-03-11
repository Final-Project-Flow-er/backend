package com.chaing.domain.settlements.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {
    RECEIPT_PDF("영수증 PDF (일별, 월별)"),
    VOUCHER_EXCEL("전표 Excel (월별)"),
    HQ_DAILY_SUMMARY_PDF("본사 일별 전체 요약 PDF"),
    HQ_MONTHLY_SUMMARY_PDF("본사 월별 전체 요약 PDF");

    private String description;
}
