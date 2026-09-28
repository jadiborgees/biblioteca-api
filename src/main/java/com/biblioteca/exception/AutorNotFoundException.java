package com.biblioteca.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AutorNotFoundException extends RuntimeException {

    public AutorNotFoundException(Long id) {
        super("Autor não encontrado: " + id);
    }
}