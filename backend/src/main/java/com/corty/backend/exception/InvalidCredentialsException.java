package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends CortyException {

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
