package com.biblioteca.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Escreve erros produzidos por filtros, no mesmo formato do GlobalExceptionHandler. */
@Component
public class ErroHttpWriter {

    private final ObjectMapper objectMapper;

    public ErroHttpWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void escrever(HttpServletRequest request, HttpServletResponse response,
                         HttpStatus status, String titulo, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                new ErroResponse(status.value(), titulo, mensagem, request.getRequestURI()));
    }
}
