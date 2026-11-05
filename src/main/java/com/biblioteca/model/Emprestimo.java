
package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_emprestimo")
@Schema(
        name = "Emprestimo",
        description = "Representa um empréstimo de livro realizado por um usuário da biblioteca."
)
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Identificador único do empréstimo.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotNull(message = "A data de empréstimo é obrigatória.")
    @PastOrPresent(message = "A data do empréstimo não pode ser futura.")
    @Schema(
            description = "Data em que o livro foi emprestado ao usuário. Não pode ser futura.",
            example = "2026-11-05",
            type = "string",
            format = "date",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate dataEmprestimo;

    @NotNull(message = "A data de devolução prevista é obrigatória.")
    @Schema(
            description = "Data prevista para a devolução do livro.",
            example = "2026-11-20",
            type = "string",
            format = "date",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate dataDevolucaoPrevista;

    @Schema(
            description = "Data efetiva em que o livro foi devolvido. Campo opcional.",
            example = "2026-11-18",
            type = "string",
            format = "date",
            nullable = true
    )
    private LocalDate dataDevolucao;

    @Enumerated(EnumType.STRING)
    @Schema(
            description = "Situação atual do empréstimo.",
            example = "ATIVO",
            implementation = StatusEmprestimo.class
    )
    private StatusEmprestimo status;

    @NotNull(message = "O usuário é obrigatório.")
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @Schema(
            description = "Usuário responsável pelo empréstimo.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Usuario usuario;

    @NotNull(message = "O livro é obrigatório.")
    @ManyToOne
    @JoinColumn(name = "livro_id")
    @Schema(
            description = "Livro associado ao empréstimo.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Livro livro;

    public Emprestimo() {}

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

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public void setDataDevolucaoPrevista(LocalDate dataDevolucaoPrevista) {
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
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
