package com.relog.relog.auth.social;

import com.relog.relog.auth.dto.SocialUserInfo;
import com.relog.relog.auth.exception.SocialAuthenticationException;
import com.relog.relog.member.entity.SocialProvider;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class KakaoAuthClient implements SocialAuthClient {

    private final WebClient webClient;

    public KakaoAuthClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://kapi.kakao.com").build();
    }

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("id")) {
                throw new SocialAuthenticationException("카카오 사용자 정보를 가져올 수 없습니다.");
            }

            String providerId = String.valueOf(response.get("id"));

            String email = null;
            String nickname = null;

            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");

                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    nickname = (String) profile.get("nickname");
                }
            }

            return SocialUserInfo.builder()
                    .provider(SocialProvider.KAKAO)
                    .providerId(providerId)
                    .email(email)
                    .nickname(nickname)
                    .build();
        } catch (SocialAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Kakao] 소셜 로그인 실패 - {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new SocialAuthenticationException("카카오 인증에 실패했습니다.");
        }
    }
}
