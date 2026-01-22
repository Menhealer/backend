package com.relog.relog.jwt;

import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenGenerator {

    private final JwtUtil jwtUtil;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public String generateAccessToken(Long memberId) {
        Date expiredTime = new Date(System.currentTimeMillis() + accessTokenExpiration);
        return jwtUtil.issue(expiredTime, memberId, List.of("ROLE_USER"), JwtType.ACCESS);
    }

    public String generateRefreshToken(Long memberId) {
        Date expiredTime = new Date(System.currentTimeMillis() + refreshTokenExpiration);
        return jwtUtil.issue(expiredTime, memberId, List.of("ROLE_USER"), JwtType.REFRESH);
    }
}
