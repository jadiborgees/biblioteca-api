package com.biblioteca.exception;

public class UnsupportedApiVersionException extends RuntimeException {
    public UnsupportedApiVersionException(String message) {
        super(message);
    }
}