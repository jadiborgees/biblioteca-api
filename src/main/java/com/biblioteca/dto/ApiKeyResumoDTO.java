
package com.biblioteca.dto;

import com.biblioteca.model.ApiKey;
import com.biblioteca.model.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "ApiKeyResumoDTO",
        description = "Representa um resumo das informações de uma chave de API, sem expor sua chave original."
)
public record ApiKeyResumoDTO(

        @Schema(
                description = "Identificador único da chave de API.",
                example = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
                description = "Nome ou identificação do usuário proprietário da chave.",
                example = "administrador",
                minLength = 1,
                maxLength = 100
        )
        String usuario,

        @Schema(
                description = "Nível de acesso associado à chave de API.",
                example = "ADMIN",
                allowableValues = {"ADMIN", "OPERADOR"},
                implementation = NivelAcesso.class
        )
        NivelAcesso nivel,

        @Schema(
                description = "Representação mascarada da chave de API, utilizada para identificação sem revelar o valor original.",
                example = "bib-********-12345",
                minLength = 1,
                maxLength = 100
        )
        String chaveMascarada,

        @Schema(
                description = "Indica se a chave de API está ativa e pode ser utilizada.",
                example = "true",
                allowableValues = {"true", "false"}
        )
        boolean ativa,

        @Schema(
                description = "Data e horário de criação da chave de API.",
                example = "2026-11-05T14:30:00",
                type = "string",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDateTime dataCriacao

) {

    public static ApiKeyResumoDTO from(ApiKey apiKey) {
        return new ApiKeyResumoDTO(
                apiKey.getId(),
                apiKey.getUsuario(),
                apiKey.getNivel(),
                apiKey.getMascara(),
                apiKey.isAtiva(),
                apiKey.getDataCriacao()
        );
    }
}
