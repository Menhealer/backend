package com.relog.relog.friendgroup.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FriendGroupNotFoundException extends BusinessException {

    public FriendGroupNotFoundException() {
        super("단체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
