package com.example.foodmap.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Service 등에서 throw한 ResponseStatusException 처리
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String msg = StringUtils.hasText(ex.getReason()) ? ex.getReason() : status.getReasonPhrase();
        ErrorResponse body = ErrorResponse.of(status, msg, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    // ✅ Java 표준 NoSuchElementException → 404 매핑
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException ex, HttpServletRequest req) {
        String msg = StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "Resource not found";
        ErrorResponse body = ErrorResponse.of(HttpStatus.NOT_FOUND, msg, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest req) {
        String message = ex.getMessage();
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST, message, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        String msg = StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "Internal Server Error";
        ErrorResponse body = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, msg, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    public static class ErrorResponse {
        private final int status;
        private final String error;
        private final String message;
        private final String path;
        private final Instant timestamp;

        private ErrorResponse(int status, String error, String message, String path, Instant timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = timestamp;
        }

        public static ErrorResponse of(HttpStatus status, String message, String path) {
            return new ErrorResponse(
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    path,
                    Instant.now()
            );
        }

        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public Instant getTimestamp() { return timestamp; }
    }
}
