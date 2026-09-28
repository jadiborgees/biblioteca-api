package com.biblioteca.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LivroNotFoundAdvice {

    @ExceptionHandler(LivroNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String livroNotFoundHandler(LivroNotFoundException ex) {
        return ex.getMessage();
    }
}