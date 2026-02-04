package com.relog.relog.auth.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SocialAuthenticationException extends BusinessException {

    public SocialAuthenticationException() {
        super("소셜 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED);
    }

    public SocialAuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
