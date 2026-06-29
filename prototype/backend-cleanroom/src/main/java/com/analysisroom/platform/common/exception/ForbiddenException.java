package com.analysisroom.platform.common.exception;

import com.analysisroom.platform.common.api.ApiCode;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(ApiCode.FORBIDDEN, message);
    }
}
