package com.relog.relog.auth.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException() {
        super("이미 사용중인 이메일입니다.", HttpStatus.CONFLICT);
    }
}
