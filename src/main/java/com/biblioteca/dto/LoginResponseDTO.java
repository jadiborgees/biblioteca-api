package com.biblioteca.dto;

public record LoginResponseDTO(
        String token,
        String email,
        String nivelAcesso
) {}
