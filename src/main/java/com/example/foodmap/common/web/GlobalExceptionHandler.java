package com.example.foodmap.common.web;

import com.example.foodmap.common.error.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) 비즈니스 예외(커스텀)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorCode ec = ex.getErrorCode();
        log.debug("[BusinessException] {} - {}", ec, ex.getMessage());
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ErrorResponse.of(ec, ex.getMessage(), req.getRequestURI()));
    }

    // 2) 도메인을 모르는 범용 NotFound
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException ex, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.NOT_FOUND;
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ErrorResponse.of(ec, ex.getMessage(), req.getRequestURI()));
    }

    // 3) 검증/바인딩 오류
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ErrorResponse.of(ec, ex.getMessage(), req.getRequestURI()));
    }

    // 4) 필수 파라미터·헤더 누락, 타입 불일치, 본문 파싱 오류
    @ExceptionHandler({
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.MISSING_PARAMETER.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.MISSING_PARAMETER, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.MISSING_HEADER.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.MISSING_HEADER, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.TYPE_MISMATCH.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.TYPE_MISMATCH, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.MESSAGE_NOT_READABLE.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.MESSAGE_NOT_READABLE, ex.getMostSpecificCause().getMessage(), req.getRequestURI()));
    }

    // 5) 무결성(중복키/외래키 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("[DataIntegrityViolation] {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(ErrorCode.DATA_INTEGRITY.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.DATA_INTEGRITY, "무결성 제약 위반", req.getRequestURI()));
    }

    // 6) HTTP 메서드/상태 기반
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus s = HttpStatus.valueOf(ex.getStatusCode().value());
        ErrorCode ec = switch (s) {
            case BAD_REQUEST -> ErrorCode.INVALID_INPUT_VALUE;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case METHOD_NOT_ALLOWED -> ErrorCode.METHOD_NOT_ALLOWED;
            case CONFLICT -> ErrorCode.DATA_INTEGRITY;
            default -> ErrorCode.INTERNAL_ERROR;
        };
        String msg = (ex.getReason() != null && !ex.getReason().isBlank()) ? ex.getReason() : ec.getMessage();
        return ResponseEntity.status(s).body(ErrorResponse.of(ec, msg, req.getRequestURI()));
    }

    // 7) 그 밖의 모든 예외
    @ExceptionHandler({ErrorResponseException.class, Exception.class})
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        log.error("[Unhandled] {}", ex.getMessage(), ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, ex.getMessage(), req.getRequestURI()));
    }
}
