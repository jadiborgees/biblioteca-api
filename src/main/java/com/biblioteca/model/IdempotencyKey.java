package com.biblioteca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    private String chave;

    private String recursoId;

    public IdempotencyKey() {
    }

    public IdempotencyKey(String chave, String recursoId) {
        this.chave = chave;
        this.recursoId = recursoId;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getRecursoId() {
        return recursoId;
    }

    public void setRecursoId(String recursoId) {
        this.recursoId = recursoId;
    }
}