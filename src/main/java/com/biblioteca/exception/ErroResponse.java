package com.biblioteca.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String erro;
    private final String mensagem;
    private final String caminho;
    private final Map<String, String> campos;

    public ErroResponse(int status, String erro, String mensagem, String caminho) {
        this(status, erro, mensagem, caminho, null);
    }

    public ErroResponse(int status, String erro, String mensagem, String caminho, Map<String, String> campos) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getErro() { return erro; }
    public String getMensagem() { return mensagem; }
    public String getCaminho() { return caminho; }
    public Map<String, String> getCampos() { return campos; }
}
