package com.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Column(unique = true, nullable = false)
    @Schema(description = "E-mail do usuário (deve ser único)", example = "joao@email.com")
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    @Schema(description = "Telefone de contato", example = "11999999999")
    private String telefone;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Schema(description = "Data de nascimento (mínimo 18 e máximo 120 anos)", example = "1995-05-12")
    private LocalDate dataNascimento;

    @OneToOne
    @JoinColumn(name = "endereco_id", referencedColumnName = "id")
    private Endereco endereco;

    public Usuario() {}

    @JsonIgnore
    @AssertTrue(message = "O usuário deve ter entre 18 e 120 anos")
    public boolean isIdadeValida() {
        if (dataNascimento == null) return true;
        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        return idade >= 18 && idade <= 120;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
}