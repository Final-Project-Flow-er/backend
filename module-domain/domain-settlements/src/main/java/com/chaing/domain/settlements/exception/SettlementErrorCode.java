package com.chaing.domain.settlements.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {

    // 일별 정산
    DAILY_SETTLEMENT_NOT_FOUND(404, "S001", "해당 일별 정산을 찾을 수 없습니다."),
    DAILY_SETTLEMENT_ALREADY_EXISTS(409, "S002", "해당 날짜의 정산이 이미 존재합니다."),
    // 월별 정산
    MONTHLY_SETTLEMENT_NOT_FOUND(404, "S003", "해당 월별 정산을 찾을 수 없습니다."),
    // 정산 확정
    INVALID_SETTLEMENT_STATUS(400, "S004", "유효하지 않은 정산 상태입니다."),
    SETTLEMENT_ALREADY_CONFIRMED(409, "S005", "이미 확정된 정산입니다."),
    SETTLEMENT_CANNOT_ROLLBACK(400, "S006", "확정요청 상태에서만 수정(되돌리기)이 가능합니다."),
    SETTLEMENT_CANNOT_FINALIZE(400, "S007", "확정요청 상태에서만 최종 확정이 가능합니다."),
    // 조정 전표
    ADJUSTMENT_NOT_FOUND(404, "S008", "해당 조정 전표를 찾을 수 없습니다."),
    INVALID_ADJUSTMENT_AMOUNT(400, "S009", "조정 금액은 0 이상이어야 합니다."),
    INVALID_VOUCHER_TYPE(400, "S010", "유효하지 않은 전표 유형입니다."),
    // 문서 (PDF/Excel)
    DOCUMENT_GENERATION_FAILED(500, "S011", "정산 문서 생성에 실패했습니다."),
    // 가맹점
    FRANCHISE_NOT_FOUND(404, "S012", "해당 가맹점을 찾을 수 없습니다.");

    private final Integer status;
    private final String code;
    private final String message;

}
