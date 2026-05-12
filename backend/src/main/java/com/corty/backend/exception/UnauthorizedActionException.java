package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedActionException extends CortyException {

    public UnauthorizedActionException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
