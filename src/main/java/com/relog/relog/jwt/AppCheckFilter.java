package com.relog.relog.jwt;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relog.relog.common.ApiResponse;
import com.relog.relog.config.FirebaseAppCheckProperties;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppCheckFilter extends OncePerRequestFilter {

    private final FirebaseAppCheckProperties appCheckProperties;
    private final ObjectMapper objectMapper;

    private static final String APP_CHECK_HEADER = "X-Firebase-AppCheck";
    private static final String JWKS_URL = "https://firebaseappcheck.googleapis.com/v1/jwks";

    private JwkProvider jwkProvider;

    @PostConstruct
    public void init() {
        if (appCheckProperties.isEnabled()) {
            this.jwkProvider = new JwkProviderBuilder(toUrl(JWKS_URL))
                    .cached(10, 24, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !appCheckProperties.isEnabled();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String appCheckToken = request.getHeader(APP_CHECK_HEADER);

        if (!StringUtils.hasText(appCheckToken)) {
            log.warn("App Check token missing: {} {}", request.getMethod(), request.getRequestURI());
            sendErrorResponse(response, "App Check 토큰이 필요합니다.");
            return;
        }

        try {
            verifyToken(appCheckToken);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("App Check token verification failed: {} {} - {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            sendErrorResponse(response, "유효하지 않은 App Check 토큰입니다.");
        }
    }

    private void verifyToken(String token) throws Exception {
        DecodedJWT jwt = JWT.decode(token);

        RSAPublicKey publicKey = (RSAPublicKey) jwkProvider.get(jwt.getKeyId()).getPublicKey();

        String projectNumber = appCheckProperties.getProjectNumber();

        JWTVerifier verifier = JWT.require(Algorithm.RSA256(publicKey, null))
                .withIssuer("https://firebaseappcheck.googleapis.com/" + projectNumber)
                .withAudience("projects/" + projectNumber)
                .acceptLeeway(0)
                .build();

        verifier.verify(token);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }

    private static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWKS URL: " + url, e);
        }
    }
}
