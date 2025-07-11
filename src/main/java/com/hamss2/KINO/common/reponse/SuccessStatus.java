package com.hamss2.KINO.common.reponse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /**
     * 200
     */
    SEND_REGISTER_SUCCESS(HttpStatus.OK, "회원가입 성공"),
    SEND_HEALTH_SUCCESS(HttpStatus.OK, "서버 상태 OK"),
    SEARCH_POST_SUCCESS(HttpStatus.OK, "게시물 조회 성공"),
    UPDATE_POST_SUCCESS(HttpStatus.OK, "게시물 수정 성공"),
    SEARCH_USER_SUCCESS(HttpStatus.OK, "사용자 조회 성공"),
    SEARCH_REVIEW_SUCCESS(HttpStatus.OK, "리뷰 조회 성공"),
    CREATE_REVIEW_SUCCESS(HttpStatus.OK, "리뷰 작성 성공"),
    SEARCH_CHAT_ROOM_SUCCESS(HttpStatus.OK, "채팅방 조회/생성 성공"),
    SEND_LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    SEND_LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),
    SEND_TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "토큰 갱신 성공"),
    SEND_USER_INFO_SUCCESS(HttpStatus.OK, "사용자 정보 조회 성공"),
    SEND_EMAIL_CHECK_SUCCESS(HttpStatus.OK, "이메일 사용 가능"),
    SEND_NICKNAME_CHECK_SUCCESS(HttpStatus.OK, "닉네임 사용 가능"),
    CREATE_POST_SUCCESS(HttpStatus.CREATED, "게시글 등록 성공"),
    GET_POST_LIST_SUCCESS(HttpStatus.OK, "물품 목록 조회 성공"),
    SEARCH_PRODUCT_SUCCESS(HttpStatus.OK, "물품 검색 성공"),
    SEND_USER_UPDATE_SUCCESS(HttpStatus.OK, "회원 정보 수정 성공"),
    SEND_PROFILE_IMAGE_UPDATE_SUCCESS(HttpStatus.OK, "프로필 사진 변경 성공"),
    GET_CHAT_ROOM_SUCCESS(HttpStatus.OK, "채팅방 생성 or 불러오기 성공"),
    GET_CHAT_LIST_SUCCESS(HttpStatus.OK, "내 채팅방 목록 조회 성공"),
    GET_CHAT_MSG_SUCCESS(HttpStatus.OK, "채팅방 메시지 조회 성공"),
    SOLD_OUT_SUCCESS(HttpStatus.OK, "판매 완료 설정 성공"),
    TOTAL_SUBS_SUCCESS(HttpStatus.OK, "전체 회원 수 조회 성공"),
    TOTAL_PRODUCT_SUCCESS(HttpStatus.OK, "전체 물품 개수 조회 성공"),
    COUNT_ISCOMPLETED_SUCCESS(HttpStatus.OK, "거래 완료 물품 조회 성공"),
    FIND_ALL_USER_SUCCESS(HttpStatus.OK, "전체 유저 조회 성공"),
    FIND_ALL_PRODUCT_SUCCESS(HttpStatus.OK, "전체 물품 조회 성공"),
    UPDATE_USER_ISACTIVE(HttpStatus.OK, "유저 활성 상태 변경 성공"),
    DELETE_PRODUCT_SUCCESS(HttpStatus.OK, "제품 삭제 성공"),
    FIND_RECENT_USER_SUCCESS(HttpStatus.OK, "최근 가입 회원 조회 성공"),
    FIND_RECENT_PRODUCT_SUCCESS(HttpStatus.OK, "최근 등록 상품 조회 성공"),
    DELETE_POST_SUCCESS(HttpStatus.OK, "게시글 삭제 성공"),
    SUCCESS_LOGIN_BUT_BAN(HttpStatus.LOCKED, "정지 된 사용자 입니다."),
    SUCCESS_USER_REPORT_FOUND(HttpStatus.OK, "신고된 사용자 조회 성공"),
    FIND_PENDING_USER_SUCCESS(HttpStatus.OK, "미처리 사용자 신고 조회 성공"),
    FIND_PENDING_POST_SUCCESS(HttpStatus.OK, "미처리 게시글 신고 조회 성공"),

    /**
     * 201
     */
    CREATE_RECRUIT_ARTICLE_SUCCESS(HttpStatus.CREATED, "게시글 등록 성공"),
    CREATE_REPORT_SUCCESS(HttpStatus.CREATED, "신고 등록 성공"),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
