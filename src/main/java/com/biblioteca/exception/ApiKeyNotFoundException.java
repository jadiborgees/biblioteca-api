package com.biblioteca.exception;

public class ApiKeyNotFoundException extends RuntimeException {

    public ApiKeyNotFoundException(Long id) {
        super("Chave de API não encontrada: " + id);
    }
}
