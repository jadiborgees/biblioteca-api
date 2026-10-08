package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "emprestimo")
@Schema(description = "Representação de um Empréstimo de livro efetuado no sistema")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do empréstimo", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "A data de empréstimo é obrigatória")
    @FutureOrPresent(message = "A data de empréstimo não pode ser no passado")
    @Schema(description = "Data em que o livro foi emprestado", example = "2026-10-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dataEmprestimo;

    @NotNull(message = "A data de devolução é obrigatória")
    @Schema(description = "Data prevista ou efetiva para a devolução do livro", example = "2026-10-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dataDevolucao;

    @NotNull(message = "O status do empréstimo é obrigatório")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Status atual do empréstimo (ex: ATIVO, DEVOLVIDO, ATRASADO)", example = "ATIVO", requiredMode = Schema.RequiredMode.REQUIRED)
    private StatusEmprestimo status;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @Schema(description = "Usuário que realizou o empréstimo", requiredMode = Schema.RequiredMode.REQUIRED)
    private Usuario usuario;

    @NotNull(message = "O livro é obrigatório")
    @ManyToOne
    @JoinColumn(name = "livro_id")
    @Schema(description = "Livro associado ao empréstimo", requiredMode = Schema.RequiredMode.REQUIRED)
    private Livro livro;

    public Emprestimo() {
    }

    // Garante que todo empréstimo nasce sempre como ATIVO na criação
    @PrePersist
    public void prePersist() {
        this.status = StatusEmprestimo.ATIVO;
        if (this.dataEmprestimo == null) {
            this.dataEmprestimo = LocalDate.now();
        }
    }

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

    // Lógica dinâmica: calcula o status em tempo real ao retornar o JSON
    public StatusEmprestimo getStatus() {
        if (this.status == StatusEmprestimo.DEVOLVIDO) {
            return StatusEmprestimo.DEVOLVIDO;
        }
        if (this.dataDevolucao != null && LocalDate.now().isAfter(this.dataDevolucao)) {
            return StatusEmprestimo.ATRASADO;
        }
        return StatusEmprestimo.ATIVO;
    }

    public void setStatus(StatusEmprestimo status) {
        this.status = status;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }
}