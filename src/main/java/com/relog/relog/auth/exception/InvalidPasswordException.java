package com.relog.relog.auth.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends BusinessException {

    public InvalidPasswordException() {
        super("현재 비밀번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
    }
}
