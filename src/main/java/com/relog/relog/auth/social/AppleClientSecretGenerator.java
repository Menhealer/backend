package com.relog.relog.auth.social;

import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppleClientSecretGenerator {

    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";
    private static final long EXPIRATION_MS = 15777000000L; // ~6 months

    private final String teamId;
    private final String keyId;
    private final String clientId;
    private final String privateKeyPem;

    public AppleClientSecretGenerator(
            @Value("${apple.team-id:}") String teamId,
            @Value("${apple.key-id:}") String keyId,
            @Value("${apple.client-id:}") String clientId,
            @Value("${apple.private-key:}") String privateKeyPem) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.clientId = clientId;
        this.privateKeyPem = privateKeyPem;
    }

    public String generate() {
        PrivateKey privateKey = parsePrivateKey();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .header()
                .keyId(keyId)
                .and()
                .issuer(teamId)
                .issuedAt(now)
                .expiration(expiration)
                .audience()
                .add(APPLE_AUDIENCE)
                .and()
                .subject(clientId)
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }

    private PrivateKey parsePrivateKey() {
        try {
            String keyContent = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\\n", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Apple private key 파싱에 실패했습니다.", e);
        }
    }
}
