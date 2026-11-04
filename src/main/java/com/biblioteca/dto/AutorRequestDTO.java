package com.biblioteca.dto;

import com.biblioteca.model.Autor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AutorRequestDTO(
        @NotBlank(message = "O nome do autor é obrigatório.")
        @Size(min = 2, max = 100, message = "O nome do autor deve ter entre 2 e 100 caracteres.")
        String nome,

        @Size(max = 60, message = "A nacionalidade deve ter no máximo 60 caracteres.")
        String nacionalidade
) {
    public Autor toEntity() {
        Autor autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);
        return autor;
    }
}
