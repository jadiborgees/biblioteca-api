
package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_livro")
@Schema(
        name = "Livro",
        description = "Representa um livro cadastrado no acervo da biblioteca."
)
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único do livro.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "O título é obrigatório.")
    @Size(
            min = 2,
            max = 150,
            message = "O título deve ter entre 2 e 150 caracteres."
    )
    @Column(nullable = false, length = 150)
    @Schema(
            description = "Título completo do livro.",
            example = "O Alienista",
            minLength = 2,
            maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String titulo;

    @NotBlank(message = "O ISBN é obrigatório.")
    @Pattern(
            regexp = "^(?:\\d{9}[\\dX]|\\d{13})$",
            message = "O ISBN deve ter 13 dígitos numéricos ou 10 caracteres (aceitando 'X' no último dígito)."
    )
    @Column(nullable = false, unique = true, length = 13)
    @Schema(
            description = "Código ISBN-10 ou ISBN-13 do livro. Deve ser único no acervo.",
            example = "9786555520012",
            pattern = "^(?:\\d{9}[\\dX]|\\d{13})$",
            minLength = 10,
            maxLength = 13,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String isbn;

    @NotNull(message = "O ano de publicação é obrigatório.")
    @Min(value = 1000, message = "Ano de publicação inválido.")
    @Column(nullable = false)
    @Schema(
            description = "Ano de publicação do livro. Deve ser igual ou superior a 1000.",
            example = "1882",
            minimum = "1000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer anoPublicacao;

    @Column(name = "capa_url", length = 2048)
    @Schema(description = "URL pública da imagem de capa do livro.", example = "https://exemplo.com/capa.jpg")
    private String capaUrl;

    @NotNull(message = "A quantidade total de exemplares é obrigatória.")
    @Min(value = 1, message = "A quantidade total deve ser de pelo menos 1 exemplar.")
    @Column(name = "quantidade_total", nullable = false, columnDefinition = "integer default 5")
    @Schema(description = "Quantidade total de exemplares cadastrados no acervo.", example = "5")
    private Integer quantidadeTotal = 5;

    @NotNull(message = "A quantidade disponível é obrigatória.")
    @Min(value = 0, message = "A quantidade disponível não pode ser negativa.")
    @Column(name = "quantidade_disponivel", nullable = false, columnDefinition = "integer default 5")
    @Schema(description = "Quantidade de exemplares disponíveis para empréstimo.", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer quantidadeDisponivel = 5;

    @NotEmpty(message = "O livro deve possuir pelo menos um autor.")
    @ManyToMany
    @JoinTable(
            name = "tb_livro_autor",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    @Schema(
            description = "Autores associados ao livro. Deve conter pelo menos um autor cadastrado.",
            requiredMode = Schema.RequiredMode.REQUIRED
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

    public String getCapaUrl() {
        return capaUrl;
    }

    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }

    public Integer getQuantidadeTotal() {
        return quantidadeTotal == null ? 5 : quantidadeTotal;
    }

    public void setQuantidadeTotal(Integer quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel == null ? getQuantidadeTotal() : quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    @PrePersist
    public void inicializarEstoque() {
        if (quantidadeTotal == null || quantidadeTotal < 1) quantidadeTotal = 5;
        if (quantidadeDisponivel == null) quantidadeDisponivel = quantidadeTotal;
    }

    public Set<Autor> getAutores() {
        return autores;
    }

    public void setAutores(Set<Autor> autores) {
        this.autores = autores;
    }
}
