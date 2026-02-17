package com.chaing.domain.settlements.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SettlementLogType {

    // 집계
    CALCULATION_STARTED, // 월별 정산 집계 시작
    CALCULATION_COMPLETED, // 월별 정산 집계 완료

    // 조정
    ADJUSTMENT_CREATED, // 조정 전표 등록
    ADJUSTMENT_UPDATED, // 조정 전표 수정
    ADJUSTMENT_DELETED, // 조정 전표 삭제

    // 정산 확정
    SETTLEMENT_CONFIRMED, // 월별 정산 확정
    SETTLEMENT_CANCELED, // 확정 취소

    // 문서
    RECEIPT_PDF_GENERATED, // 정산 영수증 PDF 생성
    VOUCHER_EXCEL_GENERATED, // 전표 Excel 생성
}
