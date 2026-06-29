package com.analysisroom.platform.common.log.service;

import com.analysisroom.platform.common.log.model.OperationLogRecord;
import com.analysisroom.platform.common.log.repository.OperationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(OperationLogRecord record) {
        operationLogRepository.insert(record);
    }
}
