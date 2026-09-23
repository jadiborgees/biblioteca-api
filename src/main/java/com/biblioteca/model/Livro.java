package com.biblioteca.model;
// Pacote onde está a classe Livro.

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// Imports necessários.

@Entity
// Define Livro como uma entidade do banco.

public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // O banco cria o ID automaticamente.
    private Long id;

    @NotBlank
    // O título não pode ficar vazio.
    private String titulo;

    @NotBlank
    // O ISBN não pode ficar vazio.
    private String isbn;

    @NotNull
    // O ano precisa ser informado.
    private Integer anoPublicacao;

    public Livro() {
    }
    // Construtor vazio usado pelo JPA.

    public Livro(String titulo, String isbn, Integer anoPublicacao) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(Integer anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }
}