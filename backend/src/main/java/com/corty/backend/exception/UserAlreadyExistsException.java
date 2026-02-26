package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends CortyException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
