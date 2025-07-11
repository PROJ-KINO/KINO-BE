package com.hamss2.KINO.common.reponse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)

public enum ErrorStatus {
    /**
     * 400 BAD_REQUEST
     */
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),
    ALREADY_REGISTER_EMAIL_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 가입된 이메일 입니다."),
    VALIDATION_EMAIL_FORMAT_EXCEPTION(HttpStatus.BAD_REQUEST, "올바른 이메일 형식이 아닙니다."),
    WRONG_PASSWORD_EXCEPTION(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다."),
    ALREADY_REGISTER_NICKNAME_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 등록된 닉네임 입니다."),
    IMAGE_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "이미지 파일이 없습니다"),
    INVALID_SEARCH_PARAMETER(HttpStatus.BAD_REQUEST, "검색 파라미터가 올바르지 않습니다."),
    VALIDATION_REQUEST_PAGENATION_EXCEPTION(HttpStatus.BAD_REQUEST, "페이지 번호와 크기는 0보다 커야 합니다."),
    POST_NOT_FOUND_EXCEPTION(HttpStatus.BAD_REQUEST, "해당 게시물이 없습니다"),
    SELLER_EQUAL_BUY(HttpStatus.BAD_REQUEST, "자신의 게시글에 채팅방을 만들수 없습니다"),
    YOU_ARE_NOT_SELLER(HttpStatus.BAD_REQUEST, "해당 게시글의 판매자가 아닙니다."),

    /**
     * 401 UNAUTHORIZED
     */
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

    /**
     * 403 FORBIDDEN
     */
    OTHERS_USER_INFO_NOT_ALLOWED_EXCEPTION(HttpStatus.FORBIDDEN, "다른 사용자의 정보에 접근이 불가능합니다."),

    /**
     * 404 NOT_FOUND
     */

    NOT_LOGIN_EXCEPTION(HttpStatus.NOT_FOUND, "로그인이 필요합니다."),
    USER_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    EMAIL_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."),
    CHAT_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 채팅방을 찾을 수 없습니다."),

    /**
     * 500 SERVER_ERROR
     */
    FAIL_UPLOAD_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패하였습니다."),
    DUPLICATE_CHAT_ROOM_CREATE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "중복된 채팅방이 생성되었습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
