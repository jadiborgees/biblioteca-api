package com.biblioteca;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_api_key")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave", nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String usuario;

    private boolean ativa = true;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    public ApiKey() {}

    public ApiKey(String key, String usuario) {
        this.key = key;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}