package com.biblioteca.dto;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record LivroRequestDTO(
        @NotBlank(message = "O título é obrigatório.")
        @Size(min = 2, max = 150, message = "O título deve ter entre 2 e 150 caracteres.")
        String titulo,

        @NotBlank(message = "O ISBN é obrigatório.")
        @Pattern(regexp = "^(?:\\d{9}[\\dX]|\\d{13})$",
                message = "O ISBN deve ter 13 dígitos numéricos ou 10 caracteres (aceitando 'X' no último dígito).")
        String isbn,

        @NotNull(message = "O ano de publicação é obrigatório.")
        @Min(value = 1000, message = "Ano de publicação inválido.")
        Integer anoPublicacao,

        @NotEmpty(message = "O livro deve possuir pelo menos um autor.")
        @Valid
        List<ReferenciaDTO> autores
) {
    /** Os autores já devem estar resolvidos (entidades existentes), fornecidos pelo controller. */
    public Livro toEntity(Set<Autor> autoresResolvidos) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setIsbn(isbn);
        livro.setAnoPublicacao(anoPublicacao);
        livro.setAutores(autoresResolvidos);
        return livro;
    }
}
