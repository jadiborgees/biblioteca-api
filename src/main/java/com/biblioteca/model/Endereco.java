package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "endereco")
@Schema(description = "Representação do Endereço associado ao usuário")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do endereço", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O campo rua não pode estar vazio")
    @Size(min = 2, max = 150, message = "A rua deve ter entre 2 e 150 caracteres")
    @Schema(description = "Nome da rua, avenida ou logradouro", example = "Avenida Paulista", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 150)
    private String rua;

    @NotBlank(message = "O campo numero não pode estar vazio")
    @Size(min = 1, max = 10, message = "O número deve ter entre 1 e 10 caracteres")
    @Schema(description = "Número do imóvel", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 10)
    private String numero;

    @NotBlank(message = "O bairro não pode estar vazio")
    @Size(min = 2, max = 100, message = "O bairro deve ter entre 2 e 100 caracteres")
    @Schema(description = "Bairro ou região", example = "Bela Vista", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
    private String bairro;

    @NotBlank(message = "A cidade não pode estar vazia")
    @Size(min = 2, max = 100, message = "A cidade deve ter entre 2 e 100 caracteres")
    @Schema(description = "Cidade", example = "São Paulo", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
    private String cidade;

    @NotBlank(message = "O estado não pode estar vazio")
    @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 caracteres")
    @Schema(description = "Sigla do estado (UF)", example = "SP", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 2)
    private String estado;

    @NotBlank(message = "O CEP não pode estar vazio")
    @Size(min = 8, max = 8, message = "O CEP deve ter exatamente 8 caracteres")
    @Schema(description = "CEP contendo exatamente 8 dígitos (somente números)", example = "01310100", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 8)
    private String cep;

    public Endereco() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
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