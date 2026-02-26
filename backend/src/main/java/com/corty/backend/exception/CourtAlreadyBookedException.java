package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class CourtAlreadyBookedException extends CortyException {
    public CourtAlreadyBookedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
