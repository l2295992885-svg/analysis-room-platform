package com.analysisroom.platform.common.exception;

import com.analysisroom.platform.common.api.ApiCode;

public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(ApiCode.UNPROCESSABLE_ENTITY, message);
    }
}
