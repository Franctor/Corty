package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends CortyException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
