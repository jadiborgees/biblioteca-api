package com.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_livro")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório.")
    @Size(min = 2, max = 150, message = "O título deve ter entre 2 e 150 caracteres.")
    private String titulo;

    @NotBlank(message = "O ISBN é obrigatório.")
    @Pattern(
            regexp = "^(?:\\d{9}[\\dX]|\\d{13})$",
            message = "O ISBN deve ter 13 dígitos numéricos ou 10 caracteres (aceitando 'X' no último dígito)."
    )
    @Column(unique = true)
    private String isbn;

    @NotNull(message = "O ano de publicação é obrigatório.")
    @Min(value = 1000, message = "Ano de publicação inválido.")
    private Integer anoPublicacao;

    @NotEmpty(message = "O livro deve possuir pelo menos um autor.")
    @ManyToMany
    @JoinTable(
            name = "tb_livro_autor",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private Set<Autor> autores = new HashSet<>();

    public Livro() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Set<Autor> getAutores() {
        return autores;
    }

    public void setAutores(Set<Autor> autores) {
        this.autores = autores;
    }
}