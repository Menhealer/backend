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
    private final RefreshTokenRepository refreshTokenRepository;

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
        String refreshToken = jwtUtil.issue(expiredTime, memberId, List.of("ROLE_USER"), JwtType.REFRESH);
        refreshTokenRepository.save(memberId, refreshToken);
        return refreshToken;
    }

    public String rotateRefreshToken(Long memberId, String oldRefreshToken) {
        String storedToken = refreshTokenRepository.findByMemberId(memberId);
        if (storedToken == null || !storedToken.equals(oldRefreshToken)) {
            return null;
        }
        return generateRefreshToken(memberId);
    }

    public void invalidateRefreshToken(Long memberId) {
        refreshTokenRepository.delete(memberId);
    }

    public boolean isRefreshTokenValid(Long memberId, String refreshToken) {
        String storedToken = refreshTokenRepository.findByMemberId(memberId);
        if (storedToken == null) {
            return false;
        }
        return storedToken.equals(refreshToken);
    }
}
