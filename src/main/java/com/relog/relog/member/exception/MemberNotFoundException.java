package com.relog.relog.member.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException() {
        super("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
