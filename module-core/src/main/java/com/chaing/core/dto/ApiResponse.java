package com.chaing.core.dto;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    // 성공 응답 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", data);
    }

    // 실패 응답
    public static <T> ApiResponse<T> fail(String message) {
        // [수정 포인트] <> 사이에 T를 명시해 주세요.
        return new ApiResponse<T>(false, message, null);
    }
}
