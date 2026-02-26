package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class FileStorageException extends CortyException {
    public FileStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
