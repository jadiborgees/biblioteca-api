
package com.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "LoginResponseDTO",
        description = "Resposta retornada após a autenticação bem-sucedida de um usuário."
)
public record LoginResponseDTO(

        @Schema(
                description = "Token de autenticação utilizado para acessar os endpoints protegidos da API.",
                example = "bib-exemplo-chave",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String token,

        @Schema(
                description = "Endereço de e-mail do usuário autenticado.",
                example = "usuario@exemplo.com",
                format = "email",
                maxLength = 254,
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String email,

        @Schema(
                description = "Nível de acesso associado ao usuário autenticado.",
                example = "ADMIN",
                allowableValues = {"ADMIN", "OPERADOR"},
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String nivelAcesso

) {}
