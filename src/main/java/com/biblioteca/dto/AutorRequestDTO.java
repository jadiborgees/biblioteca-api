
package com.biblioteca.dto;

import com.biblioteca.model.Autor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "AutorRequestDTO",
        description = "Dados necessários para cadastrar ou atualizar um autor."
)
public record AutorRequestDTO(

        @Schema(
                description = "Nome completo do autor.",
                example = "Machado de Assis",
                minLength = 2,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O nome do autor é obrigatório.")
        @Size(min = 2, max = 100,
                message = "O nome do autor deve ter entre 2 e 100 caracteres.")
        String nome,

        @Schema(
                description = "Nacionalidade do autor. Campo opcional.",
                example = "Brasileira",
                minLength = 0,
                maxLength = 60,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(max = 60,
                message = "A nacionalidade deve ter no máximo 60 caracteres.")
        String nacionalidade

) {

    public Autor toEntity() {
        Autor autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);
        return autor;
    }
}
