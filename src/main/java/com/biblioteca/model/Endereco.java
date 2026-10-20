package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "tb_endereco")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O logradouro é obrigatório")
    @Schema(description = "Logradouro / Rua", example = "Rua das Flores")
    private String logradouro;

    @NotBlank(message = "O número é obrigatório")
    @Schema(description = "Número da residência", example = "123")
    private String numero;

    @Schema(description = "Complemento", example = "Apto 101")
    private String complemento;

    @NotBlank(message = "O bairro é obrigatório")
    @Schema(description = "Bairro", example = "Centro")
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória")
    @Schema(description = "Cidade", example = "São Paulo")
    private String cidade;

    @NotBlank(message = "O estado é obrigatório")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "O estado (UF) deve conter exatamente 2 letras")
    @Schema(description = "Estado (UF) com 2 letras", example = "SP")
    private String estado;

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "O CEP deve estar no formato 00000-000 ou 00000000")
    @Schema(description = "CEP no formato 00000-000 ou 00000000", example = "01001-000")
    private String cep;

    public Endereco() {}

    public Endereco(Long id, String logradouro, String numero, String complemento, String bairro, String cidade, String estado, String cep) {
        this.id = id;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

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