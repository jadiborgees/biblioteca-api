package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Enum que representa os possíveis estados de um empréstimo de livro na biblioteca")
public enum StatusEmprestimo {

    @Schema(description = "Indica que o empréstimo está ativo e o livro ainda não foi devolvido")
    ATIVO,

    @Schema(description = "Indica que o livro já foi devolvido pelo usuário")
    DEVOLVIDO,

    @Schema(description = "Indica que o prazo de devolução do livro expirou e está em atraso")
    ATRASADO
}