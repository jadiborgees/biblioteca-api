
package com.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "LoginRequestDTO",
        description = "Dados necessários para autenticar um usuário na API."
)
public record LoginRequestDTO(

        @Schema(
                description = "Endereço de e-mail cadastrado para autenticação.",
                example = "usuario@exemplo.com",
                format = "email",
                minLength = 5,
                maxLength = 254,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(min = 5, max = 254,
                message = "O e-mail deve ter entre 5 e 254 caracteres.")
        String email,

        @Schema(
                description = "Senha cadastrada pelo usuário. Não será exibida na resposta da API.",
                example = "********",
                minLength = 1,
                maxLength = 100,
                format = "password",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 1, max = 100,
                message = "A senha deve ter entre 1 e 100 caracteres.")
        String senha

) {}
