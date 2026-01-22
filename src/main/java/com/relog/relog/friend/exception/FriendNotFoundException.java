package com.relog.relog.friend.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FriendNotFoundException extends BusinessException {

    public FriendNotFoundException() {
        super("친구를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
