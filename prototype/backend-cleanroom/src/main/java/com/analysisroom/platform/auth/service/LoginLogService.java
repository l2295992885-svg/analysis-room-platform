package com.analysisroom.platform.auth.service;

import com.analysisroom.platform.auth.model.LoginLog;
import com.analysisroom.platform.auth.repository.AuthRepository;
import com.analysisroom.platform.common.trace.TraceIdHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginLogService {

    private final AuthRepository authRepository;

    public LoginLogService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(
        Long userId,
        String username,
        String ipAddress,
        String userAgent,
        String status,
        String message
    ) {
        authRepository.insertLoginLog(new LoginLog(
            userId,
            username,
            ipAddress,
            userAgent,
            null,
            status,
            message,
            TraceIdHolder.getTraceId()
        ));
    }
}
