package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "autor")
@Schema(description = "Representação de um Autor registado na biblioteca")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único gerado automaticamente", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome do autor não pode estar vazio")
    @Size(min = 2, max = 150, message = "O nome do autor deve ter entre 2 e 150 caracteres")
    @Column(nullable = false, unique = true)
    @Schema(description = "Nome completo do autor (deve ser único)", example = "Machado de Assis", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 150)
    private String nome;

    @NotBlank(message = "A nacionalidade não pode estar vazia")
    @Size(min = 2, max = 100, message = "A nacionalidade deve ter entre 2 e 100 caracteres")
    @Schema(description = "País de origem ou nacionalidade do autor", example = "Brasileira", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
    private String nacionalidade;

    public Autor() {
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

    public String getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }
}