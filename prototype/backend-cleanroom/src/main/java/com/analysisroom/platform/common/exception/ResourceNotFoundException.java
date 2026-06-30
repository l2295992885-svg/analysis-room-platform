package com.analysisroom.platform.common.exception;

import com.analysisroom.platform.common.api.ApiCode;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ApiCode.NOT_FOUND, message);
    }
}
