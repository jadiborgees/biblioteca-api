
package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nível de acesso concedido a uma chave de API.")
public enum NivelAcesso {

    @Schema(description = "Acesso administrativo completo à API, incluindo a gestão de chaves.")
    ADMIN,

    @Schema(description = "Leitura e escrita nos recursos, exceto gestão de chaves.")
    OPERADOR;

    /**
     * Verifica se este nível pode executar o método HTTP
     * no caminho informado.
     */
    public boolean permite(String metodoHttp, String caminho) {

        if (metodoHttp == null || caminho == null) {
            return false;
        }

        String metodo = metodoHttp.toUpperCase();
        String rota = caminho.split("\\?")[0];

        // ADMIN possui acesso total.
        if (this == ADMIN) {
            return true;
        }

        // OPERADOR não pode gerenciar chaves.
        if (this == OPERADOR) {
            return !rota.equals("/api/keys")
                    && !rota.startsWith("/api/keys/");
        }

        return false;
    }
}
