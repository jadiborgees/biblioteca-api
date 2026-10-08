package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "livro")
@Schema(description = "Representação de um Livro no acervo da biblioteca")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do livro", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O título do livro não pode estar vazio")
    @Size(min = 1, max = 200, message = "O título deve ter entre 1 e 200 caracteres")
    @Schema(description = "Título oficial do livro", example = "Dom Casmurro", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 200)
    private String titulo;

    @NotBlank(message = "O ISBN não pode estar vazio")
    @Size(min = 10, max = 13, message = "O ISBN deve ter entre 10 e 13 caracteres")
    @Column(nullable = false, unique = true)
    @Schema(description = "Código ISBN único do livro (10 a 13 dígitos)", example = "9788535914849", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 10, maxLength = 13)
    private String isbn;

    @NotNull(message = "O ano de publicação é obrigatório")
    @Min(value = 1000, message = "O ano de publicação deve ser válido")
    @Schema(description = "Ano em que o livro foi publicado", example = "1899", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1000")
    private Integer anoPublicacao;

    @ManyToMany
    @JoinTable(
            name = "livro_autor",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    @Schema(description = "Lista de autores associados a este livro")
    private Set<Autor> autores = new HashSet<>();

    public Livro() {
    }

    public Livro(String titulo, String isbn, Integer anoPublicacao) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
    }

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