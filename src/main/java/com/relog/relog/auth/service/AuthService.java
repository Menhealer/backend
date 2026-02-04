package com.relog.relog.auth.service;

import com.relog.relog.auth.dto.SocialLoginRequest;
import com.relog.relog.auth.dto.SocialLoginResponse;
import com.relog.relog.auth.dto.SocialSignUpRequest;
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

    public SocialLoginResponse socialLogin(SocialLoginRequest request) {
        SocialProvider provider = parseProvider(request.getProvider());
        SocialAuthClient client = socialAuthClientFactory.getClient(provider);
        SocialUserInfo userInfo = client.getUserInfo(request.getToken());

        return memberRepository
                .findByProviderAndProviderId(provider, userInfo.getProviderId())
                .map(member -> {
                    TokenResponse tokens = createTokenResponse(member.getId());
                    return SocialLoginResponse.builder()
                            .isNewMember(false)
                            .accessToken(tokens.getAccessToken())
                            .refreshToken(tokens.getRefreshToken())
                            .build();
                })
                .orElse(SocialLoginResponse.builder()
                        .isNewMember(true)
                        .build());
    }

    @Transactional
    public TokenResponse signUp(SocialSignUpRequest request) {
        SocialProvider provider = parseProvider(request.getProvider());
        SocialAuthClient client = socialAuthClientFactory.getClient(provider);
        SocialUserInfo userInfo = client.getUserInfo(request.getToken());

        memberRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .ifPresent(member -> {
                    throw new SocialAuthenticationException("이미 가입된 회원입니다.");
                });

        RelogMember member = RelogMember.builder()
                .provider(provider)
                .providerId(userInfo.getProviderId())
                .email(userInfo.getEmail())
                .nickname(request.getNickname())
                .birthday(request.getBirthday())
                .build();

        RelogMember savedMember = memberRepository.save(member);
        return createTokenResponse(savedMember.getId());
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

    private SocialProvider parseProvider(String provider) {
        try {
            return SocialProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SocialAuthenticationException("지원하지 않는 소셜 로그인 제공자입니다.");
        }
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
