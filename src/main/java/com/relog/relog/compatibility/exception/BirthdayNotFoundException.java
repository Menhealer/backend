package com.relog.relog.compatibility.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class BirthdayNotFoundException extends BusinessException {

    public BirthdayNotFoundException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
