package com.biblioteca.dto;

import com.biblioteca.model.ApiKey;
import com.biblioteca.model.NivelAcesso;

import java.time.LocalDateTime;

public record ApiKeyResumoDTO(Long id, String usuario, NivelAcesso nivel, String chaveMascarada,
                              boolean ativa, LocalDateTime dataCriacao) {

    public static ApiKeyResumoDTO from(ApiKey apiKey) {
        return new ApiKeyResumoDTO(apiKey.getId(), apiKey.getUsuario(), apiKey.getNivel(),
                apiKey.getMascara(), apiKey.isAtiva(), apiKey.getDataCriacao());
    }
}
