
package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_api_key")
@Schema(
        name = "ApiKey",
        description = "Entidade responsável pelo armazenamento e gerenciamento das chaves de acesso à API."
)
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único da chave de API.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    // Hash SHA-256 da chave, nunca a chave original.
    @NotBlank(message = "O hash da chave é obrigatório.")
    @Size(min = 64, max = 64,
            message = "O hash deve possuir exatamente 64 caracteres.")
    @Pattern(regexp = "^[a-fA-F0-9]{64}$",
            message = "O hash deve conter somente caracteres hexadecimais.")
    @Column(name = "chave_hash", nullable = false, unique = true, length = 64)
    @Schema(
            description = "Hash SHA-256 da chave de API. Campo interno.",
            accessMode = Schema.AccessMode.WRITE_ONLY,
            hidden = true
    )
    private String chaveHash;

    @NotBlank(message = "A máscara da chave é obrigatória.")
    @Size(max = 32,
            message = "A máscara deve possuir no máximo 32 caracteres.")
    @Column(nullable = false, length = 32)
    @Schema(
            description = "Representação mascarada da chave.",
            example = "bib-********-12345",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String mascara;

    @NotBlank(message = "O usuário responsável pela chave é obrigatório.")
    @Size(min = 1, max = 100,
            message = "O nome do usuário deve ter entre 1 e 100 caracteres.")
    @Column(nullable = false, length = 100)
    @Schema(
            description = "Nome ou identificação do usuário responsável pela chave.",
            example = "Administrador"
    )
    private String usuario;

    @NotNull(message = "O nível de acesso é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(
            description = "Nível de acesso associado à chave.",
            example = "ADMIN",
            allowableValues = {"ADMIN", "OPERADOR", "LEITOR"}
    )
    private NivelAcesso nivel;

    @Schema(
            description = "Indica se a chave está ativa.",
            example = "true"
    )
    private boolean ativa = true;

    @NotNull(message = "A data de criação é obrigatória.")
    @PastOrPresent(message = "A data de criação não pode estar no futuro.")
    @Column(nullable = false)
    @Schema(
            description = "Data e hora de criação da chave.",
            example = "2026-11-05T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime dataCriacao = LocalDateTime.now();

    public ApiKey() {}

    public ApiKey(String chaveHash, String mascara, String usuario, NivelAcesso nivel) {
        this.chaveHash = chaveHash;
        this.mascara = mascara;
        this.usuario = usuario;
        this.nivel = nivel;
        this.ativa = true;
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getChaveHash() {
        return chaveHash;
    }

    public String getMascara() {
        return mascara;
    }

    public String getUsuario() {
        return usuario;
    }

    public NivelAcesso getNivel() {
        return nivel;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}
