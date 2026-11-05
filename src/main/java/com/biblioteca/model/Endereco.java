
package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "tb_endereco")
@Schema(
        name = "Endereco",
        description = "Representa o endereço de um usuário cadastrado na biblioteca."
)
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único do endereço.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "O logradouro é obrigatório.")
    @Size(min = 2, max = 150,
            message = "O logradouro deve ter entre 2 e 150 caracteres.")
    @Schema(
            description = "Nome da rua, avenida, travessa ou outro tipo de logradouro.",
            example = "Avenida Paulista",
            minLength = 2,
            maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String logradouro;

    @NotBlank(message = "O número é obrigatório.")
    @Size(min = 1, max = 20,
            message = "O número deve ter entre 1 e 20 caracteres.")
    @Schema(
            description = "Número do imóvel. Aceita valores como número, S/N ou bloco.",
            example = "123",
            minLength = 1,
            maxLength = 20,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String numero;

    @Size(max = 100,
            message = "O complemento deve ter no máximo 100 caracteres.")
    @Schema(
            description = "Complemento do endereço, como apartamento, bloco ou sala. Campo opcional.",
            example = "Apartamento 42, Bloco B",
            maxLength = 100,
            nullable = true
    )
    private String complemento;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(min = 2, max = 100,
            message = "O bairro deve ter entre 2 e 100 caracteres.")
    @Schema(
            description = "Bairro onde o imóvel está localizado.",
            example = "Bela Vista",
            minLength = 2,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(min = 2, max = 100,
            message = "A cidade deve ter entre 2 e 100 caracteres.")
    @Schema(
            description = "Município onde o imóvel está localizado.",
            example = "São Paulo",
            minLength = 2,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String cidade;

    @NotBlank(message = "O estado é obrigatório.")
    @Size(min = 2, max = 2,
            message = "O estado deve ter exatamente 2 letras (ex: SP).")
    @Pattern(
            regexp = "^[A-Za-z]{2}$",
            message = "O estado deve conter exatamente 2 letras."
    )
    @Schema(
            description = "Sigla da unidade federativa brasileira, com exatamente duas letras.",
            example = "SP",
            pattern = "^[A-Za-z]{2}$",
            minLength = 2,
            maxLength = 2,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String estado;

    @NotBlank(message = "O CEP é obrigatório.")
    @Pattern(
            regexp = "^\\d{5}-?\\d{3}$",
            message = "O CEP deve estar no formato 00000-000."
    )
    @Schema(
            description = "Código de Endereçamento Postal (CEP). Aceita os formatos 00000-000 ou 00000000.",
            example = "01310-100",
            pattern = "^\\d{5}-?\\d{3}$",
            minLength = 8,
            maxLength = 9,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String cep;

    public Endereco() {}

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }
}
