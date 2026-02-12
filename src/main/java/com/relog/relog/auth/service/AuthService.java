package com.relog.relog.auth.service;

import com.relog.relog.auth.dto.SocialLoginRequest;
import com.relog.relog.auth.dto.SocialLoginResponse;
import com.relog.relog.auth.dto.SocialSignUpRequest;
import com.relog.relog.auth.dto.SocialUserInfo;
import com.relog.relog.auth.dto.TokenResponse;
import com.relog.relog.auth.dto.WithdrawRequest;
import com.relog.relog.auth.exception.InvalidTokenException;
import com.relog.relog.auth.exception.SocialAuthenticationException;
import com.relog.relog.auth.social.AppleTokenRevokeService;
import com.relog.relog.auth.social.SocialAuthClient;
import com.relog.relog.auth.social.SocialAuthClientFactory;
import com.relog.relog.event.repository.EventRepository;
import com.relog.relog.friend.repository.FriendRepository;
import com.relog.relog.gift.repository.GiftRepository;
import com.relog.relog.jwt.JwtType;
import com.relog.relog.jwt.JwtUtil;
import com.relog.relog.jwt.TokenGenerator;
import com.relog.relog.member.dto.MemberResponse;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.entity.SocialProvider;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import com.relog.relog.storage.OciStorageService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final RelogMemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final EventRepository eventRepository;
    private final GiftRepository giftRepository;
    private final TokenGenerator tokenGenerator;
    private final JwtUtil jwtUtil;
    private final SocialAuthClientFactory socialAuthClientFactory;
    private final AppleTokenRevokeService appleTokenRevokeService;
    private final Optional<OciStorageService> ociStorageService;

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
                            .member(MemberResponse.from(member))
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
                .nickname(request.getNickname())
                .birthday(request.getBirthday())
                .build();

        RelogMember savedMember = memberRepository.save(member);
        TokenResponse tokens = createTokenResponse(savedMember.getId());
        return TokenResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .member(MemberResponse.from(savedMember))
                .build();
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

    @Transactional
    public void withdraw(Long memberId, WithdrawRequest request) {
        RelogMember member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        SocialProvider provider = parseProvider(request.getProvider());
        validateProviderMatch(member, provider);

        if (provider == SocialProvider.APPLE) {
            revokeAppleToken(request.getAuthorizationCode());
        }

        deleteProfileImage(member);
        giftRepository.deleteAll(giftRepository.findAllByMemberId(memberId));
        eventRepository.deleteAll(eventRepository.findAllByMemberId(memberId));
        friendRepository.deleteAll(friendRepository.findAllByMemberId(memberId));
        memberRepository.delete(member);
        tokenGenerator.invalidateRefreshToken(memberId);
    }

    private void validateProviderMatch(RelogMember member, SocialProvider provider) {
        if (member.getProvider() != provider) {
            throw new SocialAuthenticationException("가입한 소셜 로그인 제공자와 일치하지 않습니다.");
        }
    }

    private void revokeAppleToken(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            throw new SocialAuthenticationException("Apple 탈퇴에는 authorizationCode가 필수입니다.");
        }
        try {
            appleTokenRevokeService.revokeToken(authorizationCode);
        } catch (Exception e) {
            log.error("[Apple] Token revoke 실패: {}", e.getMessage());
            throw new SocialAuthenticationException("Apple 토큰 해제에 실패했습니다.");
        }
    }

    private void deleteProfileImage(RelogMember member) {
        if (member.getProfileImage() == null) {
            return;
        }
        ociStorageService.ifPresent(service -> service.deleteProfileImage(member.getProfileImage()));
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
