package com.relog.relog.friend.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FriendNameDuplicateException extends BusinessException {

    public FriendNameDuplicateException() {
        super("이미 존재하는 친구 이름입니다.", HttpStatus.CONFLICT);
    }
}
