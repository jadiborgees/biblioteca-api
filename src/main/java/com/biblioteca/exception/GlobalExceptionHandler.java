package com.biblioteca.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // LIVRO NÃO ENCONTRADO
    @ExceptionHandler(LivroNotFoundException.class)
    public ResponseEntity<Map<String, String>> tratarLivroNaoEncontrado(
            LivroNotFoundException exception) {

        Map<String, String> erro = new HashMap<>();
        erro.put("erro", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }


    // AUTOR NÃO ENCONTRADO
    @ExceptionHandler(AutorNotFoundException.class)
    public ResponseEntity<Map<String, String>> tratarAutorNaoEncontrado(
            AutorNotFoundException exception) {

        Map<String, String> erro = new HashMap<>();
        erro.put("erro", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }


    // USUÁRIO NÃO ENCONTRADO
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<Map<String, String>> tratarUsuarioNaoEncontrado(
            UsuarioNotFoundException exception) {

        Map<String, String> erro = new HashMap<>();
        erro.put("erro", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }


    // ENDEREÇO NÃO ENCONTRADO
    @ExceptionHandler(EnderecoNotFoundException.class)
    public ResponseEntity<Map<String, String>> tratarEnderecoNaoEncontrado(
            EnderecoNotFoundException exception) {

        Map<String, String> erro = new HashMap<>();
        erro.put("erro", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }


    // EMPRÉSTIMO NÃO ENCONTRADO
    @ExceptionHandler(EmprestimoNotFoundException.class)
    public ResponseEntity<Map<String, String>> tratarEmprestimoNaoEncontrado(
            EmprestimoNotFoundException exception) {

        Map<String, String> erro = new HashMap<>();
        erro.put("erro", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }


    // ERROS DE VALIDAÇÃO DO @VALID
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarErrosValidacao(
            MethodArgumentNotValidException exception) {

        Map<String, String> erros = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(erro ->
                        erros.put(
                                erro.getField(),
                                erro.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(erros);
    }


    // ERRO GENÉRICO
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> tratarErroGenerico(
            Exception exception) {

        // Mostra no console do IntelliJ a causa real do erro.
        exception.printStackTrace();

        Map<String, String> erro = new HashMap<>();

        erro.put(
                "erro",
                "Ocorreu um erro interno na API."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(erro);
    }
}