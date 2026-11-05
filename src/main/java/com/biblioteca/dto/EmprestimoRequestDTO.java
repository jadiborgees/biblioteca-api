
package com.biblioteca.dto;

import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import com.biblioteca.model.StatusEmprestimo;
import com.biblioteca.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

@Schema(
        name = "EmprestimoRequestDTO",
        description = "Dados necessários para cadastrar ou atualizar um empréstimo de livro."
)
public record EmprestimoRequestDTO(

        @Schema(
                description = "Data em que o empréstimo foi realizado. Não pode ser uma data futura.",
                example = "2026-11-05",
                type = "string",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A data de empréstimo é obrigatória.")
        @PastOrPresent(message = "A data do empréstimo não pode ser futura.")
        LocalDate dataEmprestimo,

        @Schema(
                description = "Data prevista para a devolução do livro.",
                example = "2026-11-20",
                type = "string",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A data de devolução prevista é obrigatória.")
        LocalDate dataDevolucaoPrevista,

        @Schema(
                description = "Data efetiva em que o livro foi devolvido. Campo opcional.",
                example = "2026-11-18",
                type = "string",
                format = "date",
                nullable = true
        )
        LocalDate dataDevolucao,

        @Schema(
                description = "Situação atual do empréstimo.",
                example = "ATIVO",
                implementation = StatusEmprestimo.class,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        StatusEmprestimo status,

        @Schema(
                description = "Referência do usuário que realizou o empréstimo. Deve conter o ID de um usuário existente.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O usuário é obrigatório.")
        @Valid
        ReferenciaDTO usuario,

        @Schema(
                description = "Referência do livro emprestado. Deve conter o ID de um livro existente.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O livro é obrigatório.")
        @Valid
        ReferenciaDTO livro

) {

    /**
     * Usuário e livro chegam como referências (apenas id);
     * o controller carrega as entidades reais.
     */
    public Emprestimo toEntity() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataEmprestimo(dataEmprestimo);
        emprestimo.setDataDevolucaoPrevista(dataDevolucaoPrevista);
        emprestimo.setDataDevolucao(dataDevolucao);
        emprestimo.setStatus(status);

        if (usuario != null) {
            Usuario referencia = new Usuario();
            referencia.setId(usuario.id());
            emprestimo.setUsuario(referencia);
        }

        if (livro != null) {
            Livro referencia = new Livro();
            referencia.setId(livro.id());
            emprestimo.setLivro(referencia);
        }

        return emprestimo;
    }
}
