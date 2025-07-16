package com.hamss2.KINO.api.auth.service;

import com.hamss2.KINO.api.auth.dto.GoogleOAuthResDto;
import com.hamss2.KINO.api.auth.dto.KakaoOAuthResDto;
import com.hamss2.KINO.api.auth.dto.LoginResDto;
import com.hamss2.KINO.api.auth.dto.NaverOAuthResDto;
import com.hamss2.KINO.api.auth.dto.SocialType;
import com.hamss2.KINO.api.auth.dto.UserInfo;
import com.hamss2.KINO.api.auth.repository.AuthRepository;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.common.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final KakaoOAuthService kakaoOAuthService;      // 4351093445
    private final NaverOAuthService naverOAuthService;      // JpUAIsJkfdP_L1J3tsDhoiiGAKQRd3r7-UgkEihV6jg
    private final GoogleOAuthService googleOAuthService;    // 106429518732962555807
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

//    public TokenDto login() {
//        // 로그인 로직을 구현합니다.
//        // 예시로, 사용자 인증 후 토큰을 생성하는 로직을 작성할 수 있습니다.
//        // 실제 구현에서는 사용자 정보를 받아와야 합니다.
//        // 예시로 빈 토큰을 반환합니다.
//        OAuth2AuthorizationCodeAuthenticationToken token = new OAuth2AuthorizationCodeAuthenticationToken();
//        return tokenProvider.generateTokenDto(token); // null은 실제 인증 객체로 대체해야 합니다.
//    }

    public String getLoginPage(SocialType provider) {
        return switch (provider) {
            case KAKAO -> kakaoOAuthService.getKakaoAuthUrl();
            case NAVER -> naverOAuthService.getNaverAuthUrl();
            case GOOGLE -> googleOAuthService.getGoogleAuthUrl();
        };
    }

    public LoginResDto socialLogin(
        SocialType provider,
        String code, // 인증 코드
        String state
    ) {

        final UserInfo info = switch (provider) {
            case KAKAO -> {
                KakaoOAuthResDto result = kakaoOAuthService.getKakaoAccessToken(code);
                yield kakaoOAuthService.getUserInfo(result.getAccess_token()).toUserInfo();
            }
            case NAVER -> {
                NaverOAuthResDto result = naverOAuthService.issueNaverAccessToken(code, state);
                yield naverOAuthService.getUserInfo(result.getAccess_token()).toUserInfo();
            }
            case GOOGLE -> {
                GoogleOAuthResDto result = googleOAuthService.issueGoogleAccessToken(code, state);
                yield googleOAuthService.getUserInfo(result.getAccess_token()).toUserInfo();
            }
        };

        User user = authRepository.findBySocialTypeandProviderId(provider, info.getProviderId())
            .orElse(User.createUser(info.getNickname(),
                info.getEmail(),
                provider,
                info.getProviderId(),
                info.getImage()));
//            .ifPresentOrElse(
//            user -> {// 이미 가입된 사용자},
//            () -> {// 신규 사용자
//                User user = User.createUser(info.getNickname(), info.getEmail(), provider, info.getProviderId(), info.getImage());
//                authRepository.save(user);
//            });

        authRepository.save(user);

        // TODO : 로그인 성공 시 토큰 발급 로직 추가

        return null;
    }


}
