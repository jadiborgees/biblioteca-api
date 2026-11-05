
package com.biblioteca.dto;

import com.biblioteca.model.ApiKey;
import com.biblioteca.model.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "ApiKeyCriadaDTO",
        description = "Representa uma chave de API criada para um usuário, incluindo seus dados e informações de segurança."
)
public record ApiKeyCriadaDTO(

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
                description = "Nível de acesso concedido à chave de API.",
                example = "ADMIN",
                allowableValues = {"ADMIN", "OPERADOR"},
                implementation = NivelAcesso.class
        )
        NivelAcesso nivel,

        @Schema(
                description = "Chave de API em texto puro. Deve ser armazenada em local seguro e não será exibida novamente.",
                example = "SUA_CHAVE_DE_API",
                accessMode = Schema.AccessMode.WRITE_ONLY
        )
        String chave,

        @Schema(
                description = "Aviso de segurança sobre o armazenamento da chave.",
                example = "Guarde esta chave em local seguro. Ela não será exibida novamente.",
                maxLength = 255
        )
        String aviso,

        @Schema(
                description = "Data e horário em que a chave de API foi criada.",
                example = "2026-11-05T14:30:00",
                type = "string",
                format = "date-time",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDateTime dataCriacao

) {

    public static ApiKeyCriadaDTO from(ApiKey apiKey, String chavePlana) {
        return new ApiKeyCriadaDTO(
                apiKey.getId(),
                apiKey.getUsuario(),
                apiKey.getNivel(),
                chavePlana,
                "Guarde esta chave em local seguro. Ela não será exibida novamente.",
                apiKey.getDataCriacao()
        );
    }
}
