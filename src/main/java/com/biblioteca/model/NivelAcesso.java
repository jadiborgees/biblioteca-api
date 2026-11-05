
package com.biblioteca.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nível de acesso concedido a uma chave de API.")
public enum NivelAcesso {

    @Schema(description = "Acesso administrativo completo à API, incluindo a gestão de chaves.")
    ADMIN;

    /**
     * Verifica se este nível pode executar o método HTTP no caminho informado.
     */
    public boolean permite(String metodoHttp, String caminho) {
        return true;
    }
}
