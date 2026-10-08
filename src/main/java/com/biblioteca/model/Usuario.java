package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Schema(description = "Representação de um Usuário registado na biblioteca")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do usuário", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome do usuário não pode estar vazio")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
    @Schema(description = "Nome completo do usuário", example = "Ana Silva", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 150)
    private String nome;

    @NotBlank(message = "O email não pode estar vazio")
    @Email(message = "O formato do email deve ser válido")
    @Column(nullable = false, unique = true)
    @Schema(description = "Endereço de email único do usuário", example = "ana.silva@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "O telefone não pode estar vazio")
    @Size(min = 10, max = 15, message = "O telefone deve ter entre 10 e 15 caracteres")
    @Schema(description = "Número de telefone ou telemóvel com DDD", example = "11988887777", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 10, maxLength = 15)
    private String telefone;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve ser no passado")
    @Schema(description = "Data de nascimento do usuário (deve ter entre 18 e 120 anos)", example = "1995-05-12", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dataNascimento;

    @NotNull(message = "O endereço é obrigatório")
    @ManyToOne
    @JoinColumn(name = "endereco_id")
    @Schema(description = "Endereço residencial associado ao usuário", requiredMode = Schema.RequiredMode.REQUIRED)
    private Endereco endereco;

    public Usuario() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}