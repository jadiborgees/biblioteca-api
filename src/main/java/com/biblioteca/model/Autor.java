package com.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do autor é obrigatório")
    private String nome;

    @ManyToMany(mappedBy = "autores")
    @JsonIgnore
    private Set<Livro> livros = new HashSet<>();


    public Autor() {
    }


    public Autor(String nome) {
        this.nome = nome;
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getNome() {
        return nome;
    }


    public void setNome(String nome) {
        this.nome = nome;
    }


    public Set<Livro> getLivros() {
        return livros;
    }


    public void setLivros(Set<Livro> livros) {
        this.livros = livros;
    }
}