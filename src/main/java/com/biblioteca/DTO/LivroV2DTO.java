package com.biblioteca.dto;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import java.util.Set;

public class LivroV2DTO {
    private Long id;
    private String titulo;
    private String isbn;
    private Integer anoPublicacao;
    private Set<Autor> autores;
    private String versaoApi;

    public LivroV2DTO(Livro livro, String versaoApi) {
        this.id = livro.getId();
        this.titulo = livro.getTitulo();
        this.isbn = livro.getIsbn();
        this.anoPublicacao = livro.getAnoPublicacao();
        this.autores = livro.getAutores();
        this.versaoApi = versaoApi;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getIsbn() { return isbn; }
    public Integer getAnoPublicacao() { return anoPublicacao; }
    public Set<Autor> getAutores() { return autores; }
    public String getVersaoApi() { return versaoApi; }
}