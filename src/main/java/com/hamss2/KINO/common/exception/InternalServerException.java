package com.hamss2.KINO.common.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BaseException{
    public InternalServerException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}