package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class ServerConfigurationException extends CortyException {
    public ServerConfigurationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
