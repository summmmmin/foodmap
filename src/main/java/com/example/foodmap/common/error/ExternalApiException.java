package com.example.foodmap.common.error;

public class ExternalApiException extends BusinessException {
    public ExternalApiException(String detail) { super(ErrorCode.EXTERNAL_API_ERROR, detail); }
}
