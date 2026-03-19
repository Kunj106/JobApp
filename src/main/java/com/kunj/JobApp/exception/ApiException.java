package com.kunj.JobApp.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException
{
    private final HttpStatus status;

    // Getter & Setter


    public HttpStatus getStatus() {
        return status;
    }

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
