package com.kaiburr.task1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ShellCommandException extends RuntimeException {

    public ShellCommandException(String message) {
        super(message);
    }
}