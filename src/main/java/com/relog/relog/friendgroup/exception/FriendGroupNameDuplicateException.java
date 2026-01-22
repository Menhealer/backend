package com.relog.relog.friendgroup.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FriendGroupNameDuplicateException extends BusinessException {

    public FriendGroupNameDuplicateException() {
        super("이미 존재하는 단체 이름입니다.", HttpStatus.CONFLICT);
    }
}
