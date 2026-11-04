package com.biblioteca.dto;

import com.biblioteca.model.NivelAcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApiKeyRequestDTO(
        @NotBlank(message = "O usuário dono da chave é obrigatório.")
        @Size(max = 100, message = "O nome do usuário deve ter no máximo 100 caracteres.")
        String usuario,

        @NotNull(message = "O nível de acesso é obrigatório (ADMIN, OPERADOR ou LEITOR).")
        NivelAcesso nivel
) {}
