package com.analysisroom.platform.common.exception;

import com.analysisroom.platform.common.api.ApiCode;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(ApiCode.UNAUTHORIZED, message);
    }
}
