package com.biblioteca.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_api_key")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Somente o hash SHA-256 é persistido; a chave em texto puro nunca é armazenada.
    @Column(name = "chave_hash", nullable = false, unique = true, length = 64)
    private String chaveHash;

    // Versão mascarada, usada apenas para identificação em listagens.
    @Column(nullable = false, length = 32)
    private String mascara;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelAcesso nivel;

    private boolean ativa = true;

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
