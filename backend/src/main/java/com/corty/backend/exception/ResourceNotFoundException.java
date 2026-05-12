package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends CortyException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
