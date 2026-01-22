package com.relog.relog.gift.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class GiftNotFoundException extends BusinessException {

    public GiftNotFoundException() {
        super("선물 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
