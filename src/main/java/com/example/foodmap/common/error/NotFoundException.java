package com.example.foodmap.common.error;

public class NotFoundException extends BusinessException {
    public NotFoundException(ErrorCode code, String detail) { super(code, detail); }
}
