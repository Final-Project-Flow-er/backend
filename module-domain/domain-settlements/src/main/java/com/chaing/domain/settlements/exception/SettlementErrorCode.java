package com.chaing.domain.settlements.exception;

import com.chaing.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {

    // 일별 정산
    DAILY_SETTLEMENT_NOT_FOUND(404, "S001", "해당 날짜의 정산 내역이 존재하지 않습니다."),
    DAILY_SETTLEMENT_PROCESSING(202, "S019", "해당 날짜의 정산 내역을 생성 중입니다. 잠시 후 확인해 주세요."),
    DAILY_SETTLEMENT_ALREADY_EXISTS(409, "S002", "해당 날짜의 정산이 이미 존재합니다."),
    // 월별 정산
    MONTHLY_SETTLEMENT_NOT_FOUND(404, "S003", "해당 월의 정산 내역이 존재하지 않습니다."),
    MONTHLY_SETTLEMENT_NOT_FINALIZED(400, "S017", "해당 월의 정산 내역은 정산 마감(매달 20일) 이후 자정에 업데이트됩니다."),
    // 정산 확정
    INVALID_SETTLEMENT_STATUS(400, "S004", "유효하지 않은 정산 상태입니다."),
    SETTLEMENT_ALREADY_CONFIRMED(409, "S005", "이미 확정된 정산입니다."),
    SETTLEMENT_CANNOT_ROLLBACK(400, "S006", "확정요청 상태에서만 수정(되돌리기)이 가능합니다."),
    SETTLEMENT_CANNOT_FINALIZE(400, "S007", "확정요청 상태에서만 최종 확정이 가능합니다."),
    // 조정 전표
    ADJUSTMENT_NOT_FOUND(404, "S008", "해당 조정 전표를 찾을 수 없습니다."),
    INVALID_ADJUSTMENT_AMOUNT(400, "S009", "조정 금액은 0 이상이어야 합니다."),
    INVALID_ADJUSTMENT_DATA(400, "S013", "유효하지 않은 조정 전표 데이터입니다.(필수값 누락 또는 중복)"),
    INVALID_VOUCHER_TYPE(400, "S010", "유효하지 않은 전표 유형입니다."),
    // 문서 (PDF/Excel)
    DOCUMENT_GENERATION_FAILED(500, "S011", "정산 문서(PDF/Excel) 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    DOCUMENT_NOT_FOUND(404, "S020", "해당 정산 문서를 찾을 수 없습니다."),
    DOCUMENT_STILL_GENERATING(404, "S021", "정산 문서가 아직 생성되지 않았습니다. 정산 처리 완료 후 다시 시도해 주세요."),
    // 가집계/임시 가공
    PROVISIONAL_SETTLEMENT_NOT_FOUND(404, "S018", "정산 내역(가집계)이 존재하지 않습니다. (매출 또는 발주 발생 후 확인 가능)"),
    SETTLEMENT_DATA_EMPTY(404, "S023", "해당 기간은 매출이나 발주 내역이 없는 정산 제외 기간(예: 휴무일)입니다."),
    // 가맹점
    FRANCHISE_NOT_FOUND(404, "S012", "해당 가맹점을 찾을 수 없습니다."),
    // 공통 검증
    INVALID_PARAMETER(400, "S014", "유효하지 않은 파라미터입니다."),
    INVALID_DATE_RANGE(400, "S015", "시작일은 종료일보다 늦을 수 없습니다."),
    INVALID_PAGINATION(400, "S016", "페이지 번호는 0 이상, 사이즈는 1 이상이어야 합니다."),
    INVALID_SETTLEMENT_ID(400, "S022", "유효하지 않은 정산 식별자(ID)입니다. 정보를 다시 확인해 주세요.");

    private final Integer status;
    private final String code;
    private final String message;

}
