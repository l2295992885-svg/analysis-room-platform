package com.analysisroom.platform.common.log.model;

import java.time.LocalDateTime;

public record OperationLogRecord(
    Long operatorUserId,
    String operatorUsername,
    String module,
    String action,
    String description,
    String methodName,
    String requestMethod,
    String requestUri,
    String requestParams,
    String responseData,
    String operationStatus,
    String errorMessage,
    String ipAddress,
    String traceId,
    LocalDateTime operationTime,
    long costMillis
) {
}
