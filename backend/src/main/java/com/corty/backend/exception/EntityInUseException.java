package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class EntityInUseException extends CortyException {
    public EntityInUseException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
