
package com.biblioteca.controller;

import com.biblioteca.dto.LoginRequestDTO;
import com.biblioteca.dto.LoginResponseDTO;
import com.biblioteca.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO requisicao) {

        LoginResponseDTO resposta = authService.login(requisicao);

        return ResponseEntity.ok(resposta);
    }
}
