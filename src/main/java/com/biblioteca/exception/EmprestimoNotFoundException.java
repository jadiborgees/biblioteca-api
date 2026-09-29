package com.biblioteca.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmprestimoNotFoundException extends RuntimeException {

    public EmprestimoNotFoundException(Long id) {
        super("Empréstimo não encontrado com o ID: " + id);
    }
}