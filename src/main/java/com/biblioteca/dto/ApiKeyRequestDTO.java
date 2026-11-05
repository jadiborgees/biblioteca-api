
package com.biblioteca.dto;

import com.biblioteca.model.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        name = "ApiKeyRequestDTO",
        description = "Dados necessários para solicitar a criação de uma chave de API."
)
public record ApiKeyRequestDTO(

        @Schema(
                description = "Nome ou identificação do usuário proprietário da chave.",
                example = "administrador",
                minLength = 1,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O usuário dono da chave é obrigatório.")
        @Size(min = 1, max = 100,
                message = "O nome do usuário deve ter entre 1 e 100 caracteres.")
        String usuario,

        @Schema(
                description = "Nível de acesso que será concedido à chave de API.",
                example = "ADMIN",
                allowableValues = {"ADMIN", "OPERADOR", "LEITOR"},
                implementation = NivelAcesso.class,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O nível de acesso é obrigatório (ADMIN, OPERADOR ou LEITOR).")
        NivelAcesso nivel

) {}
