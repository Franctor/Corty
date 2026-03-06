package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class EmptyFileException extends CortyException {
    public EmptyFileException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
