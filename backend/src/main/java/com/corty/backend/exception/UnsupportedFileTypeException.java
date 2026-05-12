package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedFileTypeException extends CortyException {

    public UnsupportedFileTypeException(String message) {
        super(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
}
