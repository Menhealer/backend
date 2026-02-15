package com.relog.relog.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();
        boolean hasAppCheck = request.getHeader("X-Firebase-AppCheck") != null;
        boolean hasAuth = request.getHeader("Authorization") != null;

        log.info("[REQUEST] {} {} | ip={} | AppCheck={} | Auth={} | query={}",
                method, uri, remoteAddr, hasAppCheck, hasAuth, query);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("[RESPONSE] {} {} | status={} | {}ms",
                    method, uri, response.getStatus(), duration);
        }
    }
}
