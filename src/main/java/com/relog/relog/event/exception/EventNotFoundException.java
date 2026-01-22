package com.relog.relog.event.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class EventNotFoundException extends BusinessException {

    public EventNotFoundException() {
        super("이벤트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
