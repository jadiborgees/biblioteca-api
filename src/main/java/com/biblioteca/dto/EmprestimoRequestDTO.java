package com.biblioteca.dto;

import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import com.biblioteca.model.StatusEmprestimo;
import com.biblioteca.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record EmprestimoRequestDTO(
        @NotNull(message = "A data de empréstimo é obrigatória.")
        @PastOrPresent(message = "A data do empréstimo não pode ser futura.")
        LocalDate dataEmprestimo,

        @NotNull(message = "A data de devolução prevista é obrigatória.")
        LocalDate dataDevolucaoPrevista,

        LocalDate dataDevolucao,

        StatusEmprestimo status,

        @NotNull(message = "O usuário é obrigatório.")
        @Valid
        ReferenciaDTO usuario,

        @NotNull(message = "O livro é obrigatório.")
        @Valid
        ReferenciaDTO livro
) {
    /** Usuário e livro chegam como referências (apenas id); o controller carrega as entidades reais. */
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
