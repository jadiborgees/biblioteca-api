package com.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Referência a um registro existente pelo seu id")
public record ReferenciaDTO(
        @NotNull(message = "O id de referência é obrigatório.")
        @Positive(message = "O id de referência deve ser positivo.")
        @Schema(example = "1")
        Long id
) {}
