package com.analysisroom.platform.common.api;

import com.analysisroom.platform.common.trace.TraceIdHolder;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
    int code,
    String message,
    T data,
    String traceId,
    OffsetDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return of(ApiCode.OK, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return of(ApiCode.OK, message, data);
    }

    public static <T> ApiResponse<T> fail(ApiCode code, String message) {
        return of(code, message, null);
    }

    public static <T> ApiResponse<T> of(ApiCode code, String message, T data) {
        return new ApiResponse<>(
            code.code(),
            message,
            data,
            TraceIdHolder.getTraceId(),
            OffsetDateTime.now()
        );
    }
}
