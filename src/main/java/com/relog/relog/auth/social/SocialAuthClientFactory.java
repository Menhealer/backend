package com.relog.relog.auth.social;

import com.relog.relog.auth.exception.SocialAuthenticationException;
import com.relog.relog.member.entity.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAuthClientFactory {

    private final KakaoAuthClient kakaoAuthClient;
    // TODO: 애플 로그인 활성화 시 주석 해제
    // private final AppleAuthClient appleAuthClient;

    public SocialAuthClient getClient(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> kakaoAuthClient;
            case APPLE -> throw new SocialAuthenticationException("애플 로그인은 현재 지원하지 않습니다.");
        };
    }
}
