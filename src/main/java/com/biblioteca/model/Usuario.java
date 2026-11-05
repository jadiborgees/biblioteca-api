
package com.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_usuario")
@Schema(
        name = "Usuario",
        description = "Representa um usuário cadastrado na biblioteca, incluindo seus dados pessoais, endereço e empréstimos."
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único do usuário.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 2, max = 100,
            message = "O nome deve ter entre 2 e 100 caracteres.")
    @Schema(
            description = "Nome completo do usuário.",
            example = "João da Silva",
            minLength = 2,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Forneça um endereço de e-mail válido.")
    @Size(min = 5, max = 254,
            message = "O e-mail deve ter entre 5 e 254 caracteres.")
    @Column(unique = true, length = 254)
    @Schema(
            description = "Endereço de e-mail do usuário. Deve ser válido e exclusivo.",
            example = "joao.silva@email.com",
            format = "email",
            minLength = 5,
            maxLength = 254,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Pattern(
            regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$",
            message = "O telefone deve ter um formato válido (ex: 11999998888)."
    )
    @Size(max = 15,
            message = "O telefone deve ter no máximo 15 caracteres.")
    @Schema(
            description = "Telefone de contato com DDD. Campo opcional.",
            example = "11999998888",
            pattern = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$",
            minLength = 10,
            maxLength = 15,
            nullable = true
    )
    private String telefone;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve ser uma data no passado.")
    @Schema(
            description = "Data de nascimento do usuário. Deve ser anterior à data atual.",
            example = "1998-05-20",
            type = "string",
            format = "date",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate dataNascimento;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "endereco_id")
    @Schema(
            description = "Endereço associado ao usuário. Pode estar ausente.",
            nullable = true
    )
    private Endereco endereco;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario")
    @Schema(
            description = "Lista de empréstimos associados ao usuário. Não é exibida diretamente na serialização JSON.",
            accessMode = Schema.AccessMode.READ_ONLY,
            hidden = true
    )
    private List<Emprestimo> emprestimos = new ArrayList<>();

    public Usuario() {}

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

    public List<Emprestimo> getEmprestimos() {
        return emprestimos;
    }

    public void setEmprestimos(List<Emprestimo> emprestimos) {
        this.emprestimos = emprestimos;
    }
}
