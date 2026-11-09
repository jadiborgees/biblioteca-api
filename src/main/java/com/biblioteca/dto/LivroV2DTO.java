
package com.biblioteca.dto;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(
        name = "LivroV2DTO",
        description = "Representação de um livro retornada pela versão 2 da API."
)
public class LivroV2DTO {

    @Schema(description = "Identificador único do livro.", example = "17",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Título completo do livro.", example = "O Alienista")
    private String titulo;

    @Schema(description = "Código ISBN-10 ou ISBN-13 do livro.")
    private String isbn;

    @Schema(description = "Ano de publicação do livro.", example = "1882")
    private Integer anoPublicacao;

    @Schema(description = "Conjunto de autores associados ao livro.")
    private Set<Autor> autores;

    @Schema(description = "Quantidade total de exemplares.", example = "5")
    private Integer quantidadeTotal;

    @Schema(description = "Quantidade de exemplares disponíveis.", example = "3")
    private Integer quantidadeDisponivel;

    @Schema(description = "Versão da API responsável por retornar os dados.",
            example = "v2")
    private String versaoApi;

    public LivroV2DTO(Livro livro, String versaoApi) {
        this.id = livro.getId();
        this.titulo = livro.getTitulo();
        this.isbn = livro.getIsbn();
        this.anoPublicacao = livro.getAnoPublicacao();
        this.autores = livro.getAutores();
        this.quantidadeTotal = livro.getQuantidadeTotal();
        this.quantidadeDisponivel = livro.getQuantidadeDisponivel();
        this.versaoApi = versaoApi;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    public Set<Autor> getAutores() {
        return autores;
    }

    public Integer getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public String getVersaoApi() {
        return versaoApi;
    }
}
