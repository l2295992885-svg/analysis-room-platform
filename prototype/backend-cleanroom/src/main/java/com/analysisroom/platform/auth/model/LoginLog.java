package com.analysisroom.platform.auth.model;

public record LoginLog(
    Long userId,
    String username,
    String ipAddress,
    String browser,
    String os,
    String status,
    String message,
    String traceId
) {
}
