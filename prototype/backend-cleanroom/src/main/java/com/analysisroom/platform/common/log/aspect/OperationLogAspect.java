package com.analysisroom.platform.common.log.aspect;

import com.analysisroom.platform.auth.model.AuthUser;
import com.analysisroom.platform.auth.repository.AuthRepository;
import com.analysisroom.platform.common.log.annotation.OperationLog;
import com.analysisroom.platform.common.log.model.OperationLogRecord;
import com.analysisroom.platform.common.log.sanitize.SensitiveDataSanitizer;
import com.analysisroom.platform.common.log.service.OperationLogService;
import com.analysisroom.platform.common.security.CurrentUserProvider;
import com.analysisroom.platform.common.trace.TraceIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogService operationLogService;
    private final SensitiveDataSanitizer sensitiveDataSanitizer;
    private final CurrentUserProvider currentUserProvider;
    private final AuthRepository authRepository;

    public OperationLogAspect(
        OperationLogService operationLogService,
        SensitiveDataSanitizer sensitiveDataSanitizer,
        CurrentUserProvider currentUserProvider,
        AuthRepository authRepository
    ) {
        this.operationLogService = operationLogService;
        this.sensitiveDataSanitizer = sensitiveDataSanitizer;
        this.currentUserProvider = currentUserProvider;
        this.authRepository = authRepository;
    }

    @Around("@annotation(com.analysisroom.platform.common.log.annotation.OperationLog)")
    public Object recordOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog operationLog = resolveAnnotation(joinPoint);
        long startNanos = System.nanoTime();
        LocalDateTime operationTime = LocalDateTime.now();
        Long operatorUserId = currentUserProvider.currentUserIdOrNull();
        String operatorUsername = resolveUsername(operatorUserId);
        HttpServletRequest request = currentRequest();
        String requestParams = operationLog.recordRequest() ? buildRequestParams(joinPoint, request) : null;

        try {
            Object result = joinPoint.proceed();
            String responseData = operationLog.recordResponse()
                ? sensitiveDataSanitizer.toSanitizedJson(result)
                : null;
            writeLog(
                operationLog,
                joinPoint,
                request,
                operatorUserId,
                operatorUsername,
                operationTime,
                elapsedMillis(startNanos),
                requestParams,
                responseData,
                "SUCCESS",
                null
            );
            return result;
        } catch (Throwable ex) {
            writeLog(
                operationLog,
                joinPoint,
                request,
                operatorUserId,
                operatorUsername,
                operationTime,
                elapsedMillis(startNanos),
                requestParams,
                null,
                "FAILED",
                ex.getMessage()
            );
            throw ex;
        }
    }

    private OperationLog resolveAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        OperationLog operationLog = AnnotationUtils.findAnnotation(method, OperationLog.class);
        if (operationLog == null) {
            throw new IllegalStateException("@OperationLog annotation not found");
        }
        return operationLog;
    }

    private void writeLog(
        OperationLog operationLog,
        ProceedingJoinPoint joinPoint,
        HttpServletRequest request,
        Long operatorUserId,
        String operatorUsername,
        LocalDateTime operationTime,
        long costMillis,
        String requestParams,
        String responseData,
        String status,
        String errorMessage
    ) {
        try {
            operationLogService.write(new OperationLogRecord(
                operatorUserId,
                operatorUsername,
                operationLog.module(),
                operationLog.action(),
                operationLog.description(),
                methodName(joinPoint),
                request == null ? null : request.getMethod(),
                request == null ? null : request.getRequestURI(),
                requestParams,
                responseData,
                status,
                errorMessage,
                request == null ? null : resolveIpAddress(request),
                TraceIdHolder.getTraceId(),
                operationTime,
                costMillis
            ));
        } catch (RuntimeException logException) {
            log.warn("Operation log write failed, traceId={}, message={}", TraceIdHolder.getTraceId(), logException.getMessage());
        }
    }

    private String buildRequestParams(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (request != null && !request.getParameterMap().isEmpty()) {
            params.put("query", request.getParameterMap());
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        Map<String, Object> methodArgs = new LinkedHashMap<>();
        for (int index = 0; index < args.length; index++) {
            String parameterName = parameterNames == null || index >= parameterNames.length
                ? "arg" + index
                : parameterNames[index];
            methodArgs.put(parameterName, args[index]);
        }
        if (!methodArgs.isEmpty()) {
            params.put("args", methodArgs);
        }
        return sensitiveDataSanitizer.toSanitizedJson(params);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String resolveUsername(Long operatorUserId) {
        if (operatorUserId == null) {
            return null;
        }
        return authRepository.findUserById(operatorUserId)
            .map(AuthUser::username)
            .orElse(null);
    }

    private String methodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private long elapsedMillis(long startNanos) {
        return Math.max(0, (System.nanoTime() - startNanos) / 1_000_000);
    }
}
