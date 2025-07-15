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

    SEARCH_MYPAGE_MAIN_SUCCESS(HttpStatus.OK, "마이페이지 메인 조회 성공"),
    SEARCH_MYPAGE_SHORTREVIEW_SUCCESS(HttpStatus.OK, "마이페이지 한줄평 조회 성공"),
    SEARCH_MYPAGE_REVIEW_SUCCESS(HttpStatus.OK, "마이페이지 리뷰 조회 성공"),
    SEARCH_MYPAGE_PICKMOVIE_SUCCESS(HttpStatus.OK, "마이페이지 찜한 영화 조회 성공"),
    SEARCH_MYPAGE_GENRE_SUCCESS(HttpStatus.OK, "마이페이지 장르 조회 성공"),
    UPDATE_USERGENRE_SUCCESS(HttpStatus.OK, "사용자 장르 업데이트 성공"),
    UPDATE_PROFILE_SUCCESS(HttpStatus.OK, "사용자 정보 업데이트 성공"),

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
