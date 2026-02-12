package com.relog.relog.auth.social;

import com.relog.relog.member.entity.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAuthClientFactory {

    private final KakaoAuthClient kakaoAuthClient;
    private final AppleAuthClient appleAuthClient;

    public SocialAuthClient getClient(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> kakaoAuthClient;
            case APPLE -> appleAuthClient;
        };
    }
}
