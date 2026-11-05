
package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tb_autor")
@Schema(
        name = "Autor",
        description = "Representa um autor cadastrado no acervo da biblioteca."
)
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único do autor.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "O nome do autor é obrigatório.")
    @Size(
            min = 2,
            max = 100,
            message = "O nome do autor deve ter entre 2 e 100 caracteres."
    )
    @Schema(
            description = "Nome completo do autor.",
            example = "Machado de Assis",
            minLength = 2,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nome;

    @Size(
            max = 60,
            message = "A nacionalidade deve ter no máximo 60 caracteres."
    )
    @Schema(
            description = "Nacionalidade do autor. Campo opcional.",
            example = "Brasileira",
            maxLength = 60,
            nullable = true
    )
    private String nacionalidade;

    public Autor() {}

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

    public String getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }
}
