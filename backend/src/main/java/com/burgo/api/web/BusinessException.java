package com.burgo.api.web;

import org.springframework.http.HttpStatus;

/** A request that can't be completed, with the HTTP status to answer with and a message safe to show. */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, message);
    }
}
