
package com.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
        name = "ReferenciaDTO",
        description = "Referência a um registro existente por meio de seu identificador único."
)
public record ReferenciaDTO(

        @Schema(
                description = "Identificador do registro existente que será associado à operação.",
                example = "1",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O id de referência é obrigatório.")
        @Positive(message = "O id de referência deve ser positivo.")
        Long id

) {}
