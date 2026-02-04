package com.relog.relog.auth.social;

import com.relog.relog.auth.dto.SocialUserInfo;
import com.relog.relog.auth.exception.SocialAuthenticationException;
import com.relog.relog.member.entity.SocialProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AppleAuthClient implements SocialAuthClient {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final WebClient webClient;
    private final String clientId;

    public AppleAuthClient(WebClient.Builder webClientBuilder,
                           @Value("${apple.client-id:}") String clientId) {
        this.webClient = webClientBuilder.build();
        this.clientId = clientId;
    }

    @Override
    public SocialUserInfo getUserInfo(String idToken) {
        try {
            Map<String, Object> header = parseHeader(idToken);
            String kid = (String) header.get("kid");

            PublicKey publicKey = getApplePublicKey(kid);

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .requireAudience(clientId)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            String providerId = claims.getSubject();
            String email = claims.get("email", String.class);

            return SocialUserInfo.builder()
                    .provider(SocialProvider.APPLE)
                    .providerId(providerId)
                    .email(email)
                    .nickname(null)
                    .build();
        } catch (SocialAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new SocialAuthenticationException("애플 인증에 실패했습니다.");
        }
    }

    private Map<String, Object> parseHeader(String idToken) {
        String headerPart = idToken.split("\\.")[0];
        byte[] decoded = Base64.getUrlDecoder().decode(headerPart);
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(decoded, Map.class);
        } catch (Exception e) {
            throw new SocialAuthenticationException("애플 토큰 헤더 파싱에 실패했습니다.");
        }
    }

    private PublicKey getApplePublicKey(String kid) {
        Map<String, Object> response = webClient.get()
                .uri(APPLE_KEYS_URL)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("keys")) {
            throw new SocialAuthenticationException("애플 공개키를 가져올 수 없습니다.");
        }

        List<Map<String, Object>> keys = (List<Map<String, Object>>) response.get("keys");
        Map<String, Object> matchingKey = keys.stream()
                .filter(key -> kid.equals(key.get("kid")))
                .findFirst()
                .orElseThrow(() -> new SocialAuthenticationException("일치하는 애플 공개키를 찾을 수 없습니다."));

        try {
            String n = (String) matchingKey.get("n");
            String e = (String) matchingKey.get("e");

            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new SocialAuthenticationException("애플 공개키 생성에 실패했습니다.");
        }
    }
}
