
package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

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

    // Somente o hash SHA-256 é persistido; a chave em texto puro nunca é armazenada.
    @Column(name = "chave_hash", nullable = false, unique = true, length = 64)
    @Schema(
            description = "Hash SHA-256 da chave de API. Campo interno, não deve ser exposto nas respostas.",
            accessMode = Schema.AccessMode.WRITE_ONLY,
            hidden = true
    )
    private String chaveHash;

    // Versão mascarada, usada apenas para identificação em listagens.
    @Column(nullable = false, length = 32)
    @Schema(
            description = "Representação mascarada da chave, utilizada para identificação em listagens.",
            example = "bib-********-12345",
            minLength = 1,
            maxLength = 32,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String mascara;

    @Column(nullable = false, length = 100)
    @Schema(
            description = "Nome ou identificação do usuário responsável pela chave.",
            example = "Administrador",
            minLength = 1,
            maxLength = 100
    )
    private String usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(
            description = "Nível de acesso associado à chave.",
            example = "ADMIN",
            allowableValues = {"ADMIN", "OPERADOR", "LEITOR"}
    )
    private NivelAcesso nivel;

    @Schema(
            description = "Indica se a chave está ativa e pode ser utilizada.",
            example = "true"
    )
    private boolean ativa = true;

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
    }

    public Long getId() { return id; }
    public String getChaveHash() { return chaveHash; }
    public String getMascara() { return mascara; }
    public String getUsuario() { return usuario; }
    public NivelAcesso getNivel() { return nivel; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}
