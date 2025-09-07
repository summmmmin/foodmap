package com.example.foodmap.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT_VALUE("E001", "잘못된 입력 값입니다.", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER("E002", "필수 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),
    MISSING_HEADER("E003", "필수 헤더가 누락되었습니다.", HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH("E004", "파라미터 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_READABLE("E005", "요청 본문을 읽을 수 없습니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("E006", "허용되지 않은 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    NOT_FOUND("E404", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DATA_INTEGRITY("E409", "데이터 무결성 오류입니다.", HttpStatus.CONFLICT),
    EXTERNAL_API_ERROR("E502", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    INTERNAL_ERROR("E999", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 도메인
    BOOKMARK_NOT_FOUND("B404", "북마크를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("U404", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PLACE_UPSERT_FAILED("P500", "장소 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
