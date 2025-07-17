package com.hamss2.KINO.api.auth.service;

import com.hamss2.KINO.api.auth.dto.GoogleOAuthResDto;
import com.hamss2.KINO.api.auth.dto.KakaoOAuthResDto;
import com.hamss2.KINO.api.auth.dto.LoginResDto;
import com.hamss2.KINO.api.auth.dto.NaverOAuthResDto;
import com.hamss2.KINO.api.auth.dto.SocialType;
import com.hamss2.KINO.api.auth.dto.UserInfo;
import com.hamss2.KINO.api.auth.repository.AuthRepository;
import com.hamss2.KINO.api.auth.repository.TokenRepository;
import com.hamss2.KINO.api.entity.RefreshToken;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.common.jwt.JwtUtils;
import com.hamss2.KINO.common.jwt.TokenDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthRepository authRepository;
    private final TokenRepository tokenRepository;
    private final JwtUtils jwtUtils;
    private final KakaoOAuthService kakaoOAuthService;      // 4351093445
    private final NaverOAuthService naverOAuthService;      // JpUAIsJkfdP_L1J3tsDhoiiGAKQRd3r7-UgkEihV6jg
    private final GoogleOAuthService googleOAuthService;    // 106429518732962555807

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

        User user = authRepository.findBySocialTypeAndProviderId(provider, info.getProviderId())
            .orElseGet(() -> {
                User newUser = User.createUser(info.getNickname(), info.getEmail(), provider,
                    info.getProviderId(), info.getImage());
                return authRepository.save(newUser);
            });

        // TODO : 로그인 성공 시 토큰 발급 로직 추가
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getUserId(),
            null, List.of(() -> "ROLE_USER"
        )); // 실제 권한 정보로 변경 필요
        TokenDto tokenDto = jwtUtils.generateTokenDto(authentication);

        if (tokenRepository.existsByUserUserId(user.getUserId())) {
            RefreshToken refreshToken = tokenRepository.findByUserUserId(user.getUserId());
            refreshToken.setToken(tokenDto.getRefreshToken());
        } else {
            RefreshToken refreshToken = RefreshToken.create(tokenDto.getRefreshToken(), user);
            tokenRepository.save(refreshToken);
        }

        return new LoginResDto(
            tokenDto.getAccessToken(),
            tokenDto.getRefreshToken()
        );
    }

    public String reissueAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info(authentication.getName());
        return jwtUtils.reissueAccessToken();
    }

    public Boolean logout(Long userId) {
        // 로그아웃 시 토큰 삭제
        if (tokenRepository.existsByUserUserId(userId)) {
            RefreshToken refreshToken = tokenRepository.findByUserUserId(userId);
            tokenRepository.delete(refreshToken);
            SecurityContextHolder.clearContext(); // SecurityContext 초기화
            return true;
        } else {
            log.warn("로그아웃 실패: 존재하지 않는 사용자 ID={}", userId);
            throw new IllegalArgumentException("로그아웃할 사용자의 토큰이 존재하지 않습니다.");
        }
    }
}
