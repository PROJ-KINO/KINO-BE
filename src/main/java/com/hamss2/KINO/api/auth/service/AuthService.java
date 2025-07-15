//package com.hamss2.KINO.api.auth.service;
//
//import com.hamss2.KINO.api.auth.repository.UserRepository;
//import com.hamss2.KINO.common.jwt.TokenDto;
//import com.hamss2.KINO.common.jwt.TokenProvider;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationToken;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//@Slf4j
//public class AuthService {
//
//    private final AuthRepository authRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final TokenProvider tokenProvider;
//
//    public TokenDto login() {
//        // 로그인 로직을 구현합니다.
//        // 예시로, 사용자 인증 후 토큰을 생성하는 로직을 작성할 수 있습니다.
//        // 실제 구현에서는 사용자 정보를 받아와야 합니다.
//        // 예시로 빈 토큰을 반환합니다.
//        OAuth2AuthorizationCodeAuthenticationToken token = new OAuth2AuthorizationCodeAuthenticationToken();
//        return tokenProvider.generateTokenDto(token); // null은 실제 인증 객체로 대체해야 합니다.
//    }
//
//
//}
