package com.example.foodmap.common.error;

public class ValidationException extends BusinessException {
    public ValidationException(String detail) { super(ErrorCode.INVALID_INPUT_VALUE, detail); }
}
