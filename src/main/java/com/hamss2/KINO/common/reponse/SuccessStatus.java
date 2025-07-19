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
    SEND_LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    SEND_LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),

    SEARCH_MYPAGE_MAIN_SUCCESS(HttpStatus.OK, "마이페이지 메인 조회 성공"),
    SEARCH_MYPAGE_SHORTREVIEW_SUCCESS(HttpStatus.OK, "마이페이지 한줄평 조회 성공"),
    SEARCH_MYPAGE_REVIEW_SUCCESS(HttpStatus.OK, "마이페이지 리뷰 조회 성공"),
    SEARCH_MYPAGE_PICKMOVIE_SUCCESS(HttpStatus.OK, "마이페이지 찜한 영화 조회 성공"),
    SEARCH_MYPAGE_GENRE_SUCCESS(HttpStatus.OK, "마이페이지 장르 조회 성공"),
    UPDATE_USERGENRE_SUCCESS(HttpStatus.OK, "사용자 장르 업데이트 성공"),
    UPDATE_PROFILE_SUCCESS(HttpStatus.OK, "사용자 정보 업데이트 성공"),
    SEARCH_ADMIN_ALLUSER_SUCCESS(HttpStatus.OK, "모든 사용자 정보 조회 성공"),
    SEARCH_ADMIN_REVIEW_SUCCESS(HttpStatus.OK, "신고 상세 리뷰 조회 성공"),
    SEARCH_ADMIN_SHORTREVIEW_SUCCESS(HttpStatus.OK, "신고 한줄평 조회 성공"),
    SEARCH_ADMIN_COMMENT_SUCCESS(HttpStatus.OK, "신고 댓글 조회 성공"),
    SEARCH_ADMIN_REVIEW_DETAIL_SUCCESS(HttpStatus.OK, "상세 리뷰 신고 조회 성공"),
    SEARCH_ADMIN_SHORTREVIEW_DETAIL_SUCCESS(HttpStatus.OK, "한줄평 신고 상세 조회 성공"),
    SEARCH_ADMIN_COMMENT_DETAIL_SUCCESS(HttpStatus.OK, "댓글 신고 상세 조회 성공"),
    USERBAN_DISABLE_SUCCESS(HttpStatus.OK, "정지 철회 성공"),
    PROCESS_REPORT_SUCCESS(HttpStatus.OK, "신고 처리 성공"),
    SYNC_TMDB_MOVIES_SUCCESS(HttpStatus.OK, "TMDB 영화 동기화 성공"),
    SYNC_TMDB_GENRES_SUCCESS(HttpStatus.OK, "TMDB 장르 동기화 성공"),
    SEND_HOME_SUCCESS(HttpStatus.OK, "홈 데이터 조회 성공"),
    SEND_USER_GENRE_SELECT_SUCCESS(HttpStatus.OK, "사용자 취향 장르 선택 저장 성공"),
    SEARCH_MYPICK_SUCCESS(HttpStatus.OK, "찜 여부 조회 성공"),
    DELETE_MYPICK_SUCCESS(HttpStatus.OK, "찜 해제 성공"),
    SEARCH_MOVIE_DETAIL_SUCCESS(HttpStatus.OK, "영화 상세정보 조회 성공"),
    SEARCH_SHORT_REVIEW_SUCCESS(HttpStatus.OK, "한줄평 조회 성공"),
    UPDATE_SHORT_REVIEW_SUCCESS(HttpStatus.OK, "한줄평 수정 성공"),
    DELETE_SHORT_REVIEW_SUCCESS(HttpStatus.OK, "한줄평 삭제 성공"),
    SEARCH_REVIEW_LIST_SUCCESS(HttpStatus.OK, "상세리뷰 조회 성공"),
    SEARCH_REVIEW_COUNT_SUCCESS(HttpStatus.OK, "상세리뷰 개수 조회 성공"),
    SEARCH_MOVIE_SUCCESS(HttpStatus.OK, "영화 검색 성공"),
    REPORT_SUCCESS(HttpStatus.OK, "신고 성공"),
    SEARCH_ALL_MOVIE_SUCCESS(HttpStatus.OK, "전체 영화 조회 성공"),
    LIKE_SHORT_REVIEW_SUCCESS(HttpStatus.OK, "한줄평 좋아요가 처리되었습니다."),
    REVIEW_WRITING_PAGE_SUCCESS(HttpStatus.OK, "리뷰 작성 페이지 조회 성공"),
    REVIEW_DETAIL_PAGE_SUCCESS(HttpStatus.OK, "리뷰 상세 페이지 조회 성공"),
    REVIEW_COMMENT_SUCCESS(HttpStatus.OK, "리뷰 댓글 조회 성공"),
    DELETE_REVIEW_SUCCESS(HttpStatus.OK, "리뷰 삭제 성공"),
    UPDATE_REVIEW_LIKE_SUCCESS(HttpStatus.OK, "리뷰 좋아요/싫어요 성공"),
    DELETE_REVIEW_COMMENT_SUCCESS(HttpStatus.OK, "리뷰 댓글 삭제 성공"),
    SEND_MY_INFO_SUCCESS(HttpStatus.OK, "내 정보 조회 성공"),
    UPDATE_REVIEW_COMMENT_SUCCESS(HttpStatus.OK, "리뷰 댓글 수정 성공"),

    /**
     * 201
     */
    CREATE_RECRUIT_ARTICLE_SUCCESS(HttpStatus.CREATED, "게시글 등록 성공"),
    CREATE_REPORT_SUCCESS(HttpStatus.CREATED, "신고 등록 성공"),
    CREATE_TOKEN_SUCCESS(HttpStatus.CREATED, "토큰 생성 성공"),
    CREATE_ACCESS_TOKEN_SUCCESS(HttpStatus.CREATED, "액세스 토큰 생성 성공"),
    CREATE_MYPICK_SUCCESS(HttpStatus.CREATED, "찜 등록 성공"),
    CREATE_SHORT_REVIEW_SUCCESS(HttpStatus.CREATED, "한줄평 등록 성공"),
    CREATE_REVIEW_SUCCESS(HttpStatus.CREATED, "리뷰 등록 성공"),
    CREATE_REVIEW_COMMENT_SUCCESS(HttpStatus.CREATED, "리뷰 댓글 생성 성공"),
    SEARCH_ALL_FOLLOWER_SUCCESS(HttpStatus.OK, "팔로워 리스트 조회 성공"),
    SEARCH_ALL_FOLLOWING_SUCCESS(HttpStatus.OK, "팔로잉 리스트 조회 성공"),
    SEARCH_STATUS_FOLLOW_SUCCESS(HttpStatus.OK, "팔로우 상태 조회 성공"),

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
