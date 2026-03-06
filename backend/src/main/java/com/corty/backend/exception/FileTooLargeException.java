package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class FileTooLargeException extends CortyException {
    public FileTooLargeException(String message) {
        super(message, HttpStatus.CONTENT_TOO_LARGE);
    }
}
