package com.analysisroom.platform.common.log.repository;

import com.analysisroom.platform.common.log.model.OperationLogRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OperationLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public OperationLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(OperationLogRecord record) {
        String sql = """
            INSERT INTO sys_operation_log (
                module_title, business_type, method_name, request_method, operator_type,
                operator_id, operator_name, request_url, request_ip, request_params,
                response_body, operation_status, error_message, trace_id, operation_time,
                cost_time_ms, remark
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(
            sql,
            limit(record.module(), 100),
            limit(record.action(), 64),
            limit(record.methodName(), 255),
            limit(record.requestMethod(), 20),
            "USER",
            record.operatorUserId(),
            limit(record.operatorUsername(), 64),
            limit(record.requestUri(), 255),
            limit(record.ipAddress(), 128),
            limit(record.requestParams(), 60000),
            limit(record.responseData(), 60000),
            record.operationStatus(),
            limit(record.errorMessage(), 60000),
            limit(record.traceId(), 128),
            record.operationTime(),
            record.costMillis(),
            limit(record.description(), 500)
        );
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
