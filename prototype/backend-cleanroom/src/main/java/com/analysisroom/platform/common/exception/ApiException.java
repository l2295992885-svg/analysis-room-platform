package com.analysisroom.platform.common.exception;

import com.analysisroom.platform.common.api.ApiCode;

public class ApiException extends RuntimeException {

    private final ApiCode code;

    public ApiException(ApiCode code, String message) {
        super(message);
        this.code = code;
    }

    public ApiCode code() {
        return code;
    }
}
