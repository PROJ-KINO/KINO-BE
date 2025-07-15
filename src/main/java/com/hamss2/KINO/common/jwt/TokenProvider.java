package com.hamss2.KINO.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

// TokenProvider 클래스는 JWT 토큰을 생성하고 검증하는 역할을 합니다.
@Component
@Slf4j
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth"; // 권한 정보를 저장하는 키
    private static final String TOKEN_TYPE = "Bearer";

    private final Key key; // JWT 서명에 사용할 비밀키
    private final long accessTokenValidityInMs; // Access Token 유효 기간 (밀리초 단위)
    private final long refreshTokenValidityInMs; // Refresh Token 유효 기간 (밀리초 단위)

    // 비밀키를 기반으로 키 객체 초기화
    public TokenProvider(
        @Value("${jwt.secret.key}") String secretKey,
        @Value("${jwt.access-token.expiration:3600000}") long accessTokenValidityInMs,
        @Value("${jwt.refresh-token.expiration:604800000}") long refreshTokenValidityInMs
    ) {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT Secret is required");
        }

        if (secretKey.length() < 64) { // HS512 알고리즘을 사용하려면 최소 64바이트가 필요
            throw new IllegalArgumentException(
                "JWT Secret must be at least 64 characters for HS512 algorithm");
        }

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenValidityInMs = accessTokenValidityInMs;
        this.refreshTokenValidityInMs = refreshTokenValidityInMs;
    }

    // 토큰 생성 메서드
    public TokenDto generateTokenDto(Authentication authentication) {
        // 인증 객체에서 권한 정보 추출
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        // 현재 시간과 토큰 만료 시간 계산
        Instant now = Instant.now();
        Date accessTokenExpiresIn = Date.from(now.plus(Duration.ofMillis(accessTokenValidityInMs)));
        Date refreshTokenExpiresIn = Date.from(
            now.plus(Duration.ofMillis(refreshTokenValidityInMs)));

        // Access Token 생성
        String accessToken = Jwts.builder()
            .setSubject(authentication.getName()) // 사용자명 설정, 이메일이 들어 있음
            .claim(AUTHORITIES_KEY, authorities)  // 권한 정보 저장
            .setIssuedAt(Date.from(now))
            .setExpiration(accessTokenExpiresIn)  // 만료 시간 설정
            .signWith(key, SignatureAlgorithm.HS512) // 서명 방식 설정
            .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
            .setSubject(authentication.getName()) // 사용자명 설정, 이메일이 들어 있음
            .claim(AUTHORITIES_KEY, authorities)  // 권한 정보 저장
            .setIssuedAt(Date.from(now))
            .setExpiration(refreshTokenExpiresIn)  // 만료 시간 설정
            .signWith(key, SignatureAlgorithm.HS512) // 서명 방식 설정
            .compact();

        // 결과를 DTO로 반환
        return TokenDto.builder()
            .grantType(TOKEN_TYPE)
            .accessToken(accessToken)
            .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
            .refreshToken(refreshToken)
            .refreshTokenExpiresIn(refreshTokenExpiresIn.getTime())
            .build();
    }

    // 토큰에서 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        // 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 인증 객체 생성 후 반환
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.info("JWT 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    // 토큰의 Claims(내용) 추출
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}