package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

@Entity
@Table(name = "tb_emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data de empréstimo é obrigatória")
    @PastOrPresent(message = "A data de empréstimo não pode estar no futuro")
    @Schema(description = "Data do empréstimo", example = "2026-10-01")
    private LocalDate dataEmprestimo;

    @Schema(description = "Data prevista para devolução", example = "2026-10-15")
    private LocalDate dataDevolucao;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Status do empréstimo", example = "ATIVO")
    private StatusEmprestimo status;

    @ManyToOne
    @JoinColumn(name = "livro_id")
    @NotNull(message = "O livro é obrigatório")
    private Livro livro;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @NotNull(message = "O usuário é obrigatório")
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        if (this.dataEmprestimo == null) {
            this.dataEmprestimo = LocalDate.now();
        }
        if (this.status == null) {
            this.status = StatusEmprestimo.ATIVO;
        }
    }

    // Construtores
    public Emprestimo() {}

    public Emprestimo(Long id, LocalDate dataEmprestimo, LocalDate dataDevolucao, StatusEmprestimo status, Livro livro, Usuario usuario) {
        this.id = id;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = dataDevolucao;
        this.status = status;
        this.livro = livro;
        this.usuario = usuario;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDate dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public void setStatus(StatusEmprestimo status) {
        this.status = status;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}