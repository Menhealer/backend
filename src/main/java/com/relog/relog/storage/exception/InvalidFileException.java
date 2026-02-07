package com.relog.relog.storage.exception;

import com.relog.relog.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidFileException extends BusinessException {

    public InvalidFileException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
