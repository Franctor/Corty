package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidBookingTermException extends CortyException {
    public InvalidBookingTermException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
