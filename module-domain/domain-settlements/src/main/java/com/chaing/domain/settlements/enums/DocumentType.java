package com.chaing.domain.settlements.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {
    RECEIPT_PDF("영수증 PDF (일별, 월별)"),
    VOUCHER_EXCEL("전표 Excel (월별)");

    private String description;
}
