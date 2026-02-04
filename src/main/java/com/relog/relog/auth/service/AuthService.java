package com.relog.relog.auth.service;

import com.relog.relog.auth.dto.SocialLoginRequest;
import com.relog.relog.auth.dto.SocialUserInfo;
import com.relog.relog.auth.dto.TokenResponse;
import com.relog.relog.auth.exception.InvalidTokenException;
import com.relog.relog.auth.exception.SocialAuthenticationException;
import com.relog.relog.auth.social.SocialAuthClient;
import com.relog.relog.auth.social.SocialAuthClientFactory;
import com.relog.relog.jwt.JwtType;
import com.relog.relog.jwt.JwtUtil;
import com.relog.relog.jwt.TokenGenerator;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.entity.SocialProvider;
import com.relog.relog.member.repository.RelogMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final RelogMemberRepository memberRepository;
    private final TokenGenerator tokenGenerator;
    private final JwtUtil jwtUtil;
    private final SocialAuthClientFactory socialAuthClientFactory;

    @Transactional
    public TokenResponse socialLogin(SocialLoginRequest request) {
        SocialProvider provider;
        try {
            provider = SocialProvider.valueOf(request.getProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SocialAuthenticationException("지원하지 않는 소셜 로그인 제공자입니다.");
        }

        SocialAuthClient client = socialAuthClientFactory.getClient(provider);
        SocialUserInfo userInfo = client.getUserInfo(request.getToken());

        RelogMember member = memberRepository
                .findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> createMember(userInfo));

        return createTokenResponse(member.getId());
    }

    public TokenResponse refresh(String refreshToken) {
        validateRefreshToken(refreshToken);

        Long memberId = jwtUtil.getMemberId(refreshToken);
        validateStoredRefreshToken(memberId, refreshToken);

        String newAccessToken = tokenGenerator.generateAccessToken(memberId);
        String newRefreshToken = tokenGenerator.rotateRefreshToken(memberId, refreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(Long memberId) {
        tokenGenerator.invalidateRefreshToken(memberId);
    }

    private RelogMember createMember(SocialUserInfo userInfo) {
        RelogMember member = RelogMember.builder()
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .email(userInfo.getEmail())
                .nickname(generateDefaultNickname(userInfo))
                .build();
        return memberRepository.save(member);
    }

    private String generateDefaultNickname(SocialUserInfo userInfo) {
        if (userInfo.getNickname() != null && !userInfo.getNickname().isBlank()) {
            return userInfo.getNickname();
        }
        return "사용자" + System.currentTimeMillis() % 100000;
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken, JwtType.REFRESH)) {
            throw new InvalidTokenException();
        }
    }

    private void validateStoredRefreshToken(Long memberId, String refreshToken) {
        if (!tokenGenerator.isRefreshTokenValid(memberId, refreshToken)) {
            throw new InvalidTokenException();
        }
    }

    private TokenResponse createTokenResponse(Long memberId) {
        return TokenResponse.builder()
                .accessToken(tokenGenerator.generateAccessToken(memberId))
                .refreshToken(tokenGenerator.generateRefreshToken(memberId))
                .build();
    }
}
