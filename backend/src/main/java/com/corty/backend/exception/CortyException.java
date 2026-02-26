package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class CortyException extends RuntimeException {
    private final HttpStatus status;

    public CortyException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
