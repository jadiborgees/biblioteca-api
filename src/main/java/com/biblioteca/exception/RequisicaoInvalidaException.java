package com.biblioteca.exception;

/** Dado enviado é sintaticamente válido, mas inaceitável pelas regras de entrada (retorna 400). */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
