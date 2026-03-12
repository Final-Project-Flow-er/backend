package com.chaing.domain.settlements.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {
    RECEIPT_PDF("영수증 PDF (일별, 월별)"),
    VOUCHER_EXCEL("전표 Excel (월별)"),
    HQ_DAILY_SUM("본사 일별 전체 요약 PDF"),
    HQ_MONTHLY_SUM("본사 월별 전체 요약 PDF"),
    PROVISIONAL_RECEIPT_PDF("정산 영수증 PDF (미리보기/가집계)"),
    PROVISIONAL_VOUCHER_EXCEL("전표 Excel (미리보기/가집계)");

    private String description;
}
