
package com.biblioteca.dto;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

@Schema(
        name = "LivroRequestDTO",
        description = "Dados necessários para cadastrar ou atualizar um livro na biblioteca."
)
public record LivroRequestDTO(

        @Schema(
                description = "Título completo do livro.",
                example = "O Alienista",
                minLength = 2,
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O título é obrigatório.")
        @Size(min = 2, max = 150,
                message = "O título deve ter entre 2 e 150 caracteres.")
        String titulo,

        @Schema(
                description = "Código ISBN-10 ou ISBN-13 do livro. No ISBN-10, o último caractere pode ser X.",
                example = "9786555520012",
                pattern = "^(?:\\d{9}[\\dX]|\\d{13})$",
                minLength = 10,
                maxLength = 13,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O ISBN é obrigatório.")
        @Pattern(
                regexp = "^(?:\\d{9}[\\dX]|\\d{13})$",
                message = "O ISBN deve ter 13 dígitos numéricos ou 10 caracteres (aceitando 'X' no último dígito)."
        )
        String isbn,

        @Schema(
                description = "Ano de publicação do livro. Deve ser igual ou superior a 1000.",
                example = "1882",
                minimum = "1000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O ano de publicação é obrigatório.")
        @Min(value = 1000, message = "Ano de publicação inválido.")
        Integer anoPublicacao,

        @Schema(
                description = "Lista de referências dos autores do livro. Deve conter pelo menos um autor cadastrado. Quantidade mínima: 1 autor.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "O livro deve possuir pelo menos um autor.")
        @Valid
        List<ReferenciaDTO> autores

) {

    /**
     * Os autores já devem estar resolvidos (entidades existentes),
     * fornecidos pelo controller.
     */
    public Livro toEntity(Set<Autor> autoresResolvidos) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setIsbn(isbn);
        livro.setAnoPublicacao(anoPublicacao);
        livro.setAutores(autoresResolvidos);
        return livro;
    }
}
