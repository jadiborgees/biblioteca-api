package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nível de acesso concedido a uma chave de API")
public enum NivelAcesso {

    @Schema(description = "Acesso total, incluindo a gestão de chaves de API")
    ADMIN,

    @Schema(description = "Leitura e escrita em todos os recursos, exceto a gestão de chaves")
    OPERADOR,

    @Schema(description = "Somente leitura (GET) nos recursos protegidos")
    LEITOR;

    /** Verifica se este nível pode executar o método HTTP no caminho informado. */
    public boolean permite(String metodoHttp, String caminho) {
        boolean gestaoDeChaves = caminho.startsWith("/api/keys");
        boolean leitura = "GET".equalsIgnoreCase(metodoHttp) || "HEAD".equalsIgnoreCase(metodoHttp);
        return switch (this) {
            case ADMIN -> true;
            case OPERADOR -> !gestaoDeChaves;
            case LEITOR -> leitura && !gestaoDeChaves;
        };
    }
}
