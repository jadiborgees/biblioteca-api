package com.biblioteca.dto;

import com.biblioteca.model.ApiKey;
import com.biblioteca.model.NivelAcesso;

import java.time.LocalDateTime;

public record ApiKeyCriadaDTO(Long id, String usuario, NivelAcesso nivel, String chave,
                              String aviso, LocalDateTime dataCriacao) {

    public static ApiKeyCriadaDTO from(ApiKey apiKey, String chavePlana) {
        return new ApiKeyCriadaDTO(apiKey.getId(), apiKey.getUsuario(), apiKey.getNivel(), chavePlana,
                "Guarde esta chave em local seguro. Ela não será exibida novamente.", apiKey.getDataCriacao());
    }
}
