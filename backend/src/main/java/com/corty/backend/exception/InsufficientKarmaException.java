package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class InsufficientKarmaException extends CortyException {

    public InsufficientKarmaException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
