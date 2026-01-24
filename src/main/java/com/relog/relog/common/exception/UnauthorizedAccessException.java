package com.relog.relog.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BusinessException {

    public UnauthorizedAccessException() {
        super("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
}
