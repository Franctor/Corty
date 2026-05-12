package com.corty.backend.exception;

import org.springframework.http.HttpStatus;

public class FriendshipException extends CortyException {

    public FriendshipException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
