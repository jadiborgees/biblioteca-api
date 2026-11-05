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

    private final ApiKeyService apiKeyService;

    public AuthService(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    public LoginResponseDTO login(LoginRequestDTO requisicao) {

        boolean emailValido =
                emailAdmin.equalsIgnoreCase(requisicao.email().trim());

        boolean senhaValida = MessageDigest.isEqual(
                senhaAdmin.getBytes(StandardCharsets.UTF_8),
                requisicao.senha().getBytes(StandardCharsets.UTF_8)
        );

        if (!emailValido || !senhaValida) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "E-mail ou senha incorretos."
            );
        }

        ApiKeyService.ChaveGerada chave =
                apiKeyService.gerar(emailAdmin, NivelAcesso.ADMIN);

        return new LoginResponseDTO(
                chave.chavePlana(),
                emailAdmin,
                NivelAcesso.ADMIN.name()
        );
    }
}
