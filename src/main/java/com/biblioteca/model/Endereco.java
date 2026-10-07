package com.biblioteca.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "endereco")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo rua não pode estar vazio")
    @Size(min = 2, max = 150, message = "A rua deve ter entre 2 e 150 caracteres")
    private String rua;

    @NotBlank(message = "O campo numero não pode estar vazio")
    @Size(min = 1, max = 10, message = "O número deve ter entre 1 e 10 caracteres")
    private String numero;

    @NotBlank(message = "O bairro não pode estar vazio")
    @Size(min = 2, max = 100, message = "O bairro deve ter entre 2 e 100 caracteres")
    private String bairro;

    @NotBlank(message = "A cidade não pode estar vazia")
    @Size(min = 2, max = 100, message = "A cidade deve ter entre 2 e 100 caracteres")
    private String cidade;

    @NotBlank(message = "O estado não pode estar vazio")
    @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 caracteres")
    private String estado;

    @NotBlank(message = "O CEP não pode estar vazio")
    @Size(min = 8, max = 8, message = "O CEP deve ter exatamente 8 caracteres")
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