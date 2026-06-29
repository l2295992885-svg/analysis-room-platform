package com.analysisroom.platform.common.exception;

import com.analysisroom.platform.common.api.ApiCode;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(ApiCode.BAD_REQUEST, message);
    }
}
