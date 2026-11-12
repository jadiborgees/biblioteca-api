
package com.biblioteca.service;

import com.biblioteca.dto.LoginRequestDTO;
import com.biblioteca.dto.LoginResponseDTO;
import com.biblioteca.model.NivelAcesso;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuthService {

    @Value("${biblioteca.admin.email}")
    private String emailAdmin;

    @Value("${biblioteca.admin.password}")
    private String senhaAdmin;

    @Value("${biblioteca.operador.email}")
    private String emailOperador;

    @Value("${biblioteca.operador.password}")
    private String senhaOperador;

    private final ApiKeyService apiKeyService;

    public AuthService(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    public LoginResponseDTO login(LoginRequestDTO requisicao) {

        String emailInformado = requisicao.email().trim();
        String senhaInformada = requisicao.senha();

        boolean emailAdminValido =
                emailAdmin.equalsIgnoreCase(emailInformado);

        boolean senhaAdminValida =
                MessageDigest.isEqual(
                        senhaAdmin.getBytes(StandardCharsets.UTF_8),
                        senhaInformada.getBytes(StandardCharsets.UTF_8)
                );

        boolean emailOperadorValido =
                emailOperador.equalsIgnoreCase(emailInformado);

        boolean senhaOperadorValida =
                MessageDigest.isEqual(
                        senhaOperador.getBytes(StandardCharsets.UTF_8),
                        senhaInformada.getBytes(StandardCharsets.UTF_8)
                );

        NivelAcesso nivel;
        String emailAutenticado;

        if (emailAdminValido && senhaAdminValida) {

            nivel = NivelAcesso.ADMIN;
            emailAutenticado = emailAdmin;

        } else if (emailOperadorValido && senhaOperadorValida) {

            nivel = NivelAcesso.OPERADOR;
            emailAutenticado = emailOperador;

        } else {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "E-mail ou senha incorretos."
            );
        }

        ApiKeyService.ChaveGerada chave =
                apiKeyService.gerar(emailAutenticado, nivel);

        return new LoginResponseDTO(
                chave.chavePlana(),
                emailAutenticado,
                nivel.name()
        );
    }
}
