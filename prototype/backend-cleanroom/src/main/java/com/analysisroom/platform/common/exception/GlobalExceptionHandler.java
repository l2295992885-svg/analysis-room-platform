package com.analysisroom.platform.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import com.analysisroom.platform.common.api.ApiCode;
import com.analysisroom.platform.common.api.ApiResponse;
import com.analysisroom.platform.common.trace.TraceIdHolder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        log.warn("API exception, traceId={}, code={}, message={}", TraceIdHolder.getTraceId(), ex.code().code(), ex.getMessage());
        return build(ex.code(), ex.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLogin(NotLoginException ex) {
        return build(ApiCode.UNAUTHORIZED, "未登录或登录已过期");
    }

    @ExceptionHandler(SaTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleSaTokenException(SaTokenException ex) {
        log.warn("Sa-Token exception, traceId={}, message={}", TraceIdHolder.getTraceId(), ex.getMessage());
        return build(ApiCode.UNAUTHORIZED, "认证失败");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return build(ApiCode.BAD_REQUEST, firstFieldErrorMessage(ex));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        return build(ApiCode.BAD_REQUEST, firstFieldErrorMessage(ex));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
            .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .findFirst()
            .orElse("Request parameter validation failed");
        return build(ApiCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return build(ApiCode.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(ApiCode.BAD_REQUEST, "Invalid parameter: " + ex.getName());
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex) {
        return build(ApiCode.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception, traceId={}", TraceIdHolder.getTraceId(), ex);
        return build(ApiCode.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<ApiResponse<Void>> build(ApiCode code, String message) {
        return ResponseEntity.status(code.httpStatus()).body(ApiResponse.fail(code, message));
    }

    private String firstFieldErrorMessage(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(this::formatFieldError)
            .orElse("Request body validation failed");
    }

    private String firstFieldErrorMessage(BindException ex) {
        String fieldMessage = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(this::formatFieldError)
            .orElse(null);
        if (fieldMessage != null) {
            return fieldMessage;
        }
        return ex.getAllErrors().stream()
            .map(error -> error.getDefaultMessage() == null ? "Request parameter validation failed" : error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    }

    private String formatFieldError(FieldError error) {
        String message = error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage();
        return error.getField() + ": " + message;
    }
}
