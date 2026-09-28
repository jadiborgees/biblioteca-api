package com.biblioteca.exception;

public class LivroNotFoundException extends RuntimeException {

    public LivroNotFoundException(Long id) {
        super("Livro não encontrado: " + id);
    }
}