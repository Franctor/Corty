package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class AccountStatusException extends CortyException {
    public AccountStatusException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
