package com.relog.relog.auth.social;

import com.relog.relog.auth.exception.SocialAuthenticationException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class AppleTokenRevokeService {

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";

    private final WebClient webClient;
    private final AppleClientSecretGenerator secretGenerator;
    private final String clientId;

    public AppleTokenRevokeService(
            WebClient.Builder webClientBuilder,
            AppleClientSecretGenerator secretGenerator,
            @Value("${apple.client-id:}") String clientId) {
        this.webClient = webClientBuilder.build();
        this.secretGenerator = secretGenerator;
        this.clientId = clientId;
    }

    public void revokeToken(String authorizationCode) {
        String clientSecret = secretGenerator.generate();
        String refreshToken = exchangeCodeForRefreshToken(authorizationCode, clientSecret);
        revokeRefreshToken(refreshToken, clientSecret);
    }

    private String exchangeCodeForRefreshToken(String authorizationCode, String clientSecret) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", authorizationCode);
        formData.add("grant_type", "authorization_code");

        Map<String, Object> response = webClient.post()
                .uri(APPLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("refresh_token")) {
            throw new SocialAuthenticationException("Apple 토큰 교환에 실패했습니다.");
        }

        return (String) response.get("refresh_token");
    }

    private void revokeRefreshToken(String refreshToken, String clientSecret) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("token", refreshToken);
        formData.add("token_type_hint", "refresh_token");

        webClient.post()
                .uri(APPLE_REVOKE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .toBodilessEntity()
                .block();

        log.info("[Apple] Token revoke 성공");
    }
}
