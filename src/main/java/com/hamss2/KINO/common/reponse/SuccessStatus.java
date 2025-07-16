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
    SYNC_TMDB_MOVIES_SUCCESS(HttpStatus.OK, "TMDB 영화 동기화 성공"),
    SYNC_TMDB_GENRES_SUCCESS(HttpStatus.OK, "TMDB 장르 동기화 성공"),
    SEND_HOME_SUCCESS(HttpStatus.OK, "홈 데이터 조회 성공"),
    SEND_USER_GENRE_SELECT_SUCCESS(HttpStatus.OK, "사용자 취향 장르 선택 저장 성공"),

    /**
     * 201
     */
    CREATE_RECRUIT_ARTICLE_SUCCESS(HttpStatus.CREATED, "게시글 등록 성공"),
    CREATE_REPORT_SUCCESS(HttpStatus.CREATED, "신고 등록 성공"),

    /**
     * 302 Found
     */
    REDIRECT_OAUTH_PAGE_SUCCESS(HttpStatus.FOUND, "OAuth 인증 페이지로 리다이렉트"),
    REDIRECT_OAUTH_SUCCESS(HttpStatus.FOUND, "OAuth 인증 성공");

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
