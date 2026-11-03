package com.biblioteca.controller;

import com.biblioteca.model.ApiKey;
import com.biblioteca.repository.ApiKeyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyController(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @PostMapping
    public ResponseEntity<?> generateKey(@RequestBody(required = false) Map<String, String> request) {
        String clientName = (request != null && request.containsKey("clientName"))
                ? request.get("clientName")
                : "Cliente Anonimo";

        String rawKey = "key_" + UUID.randomUUID().toString().replace("-", "");
        String keyHash = hashKey(rawKey);

        ApiKey apiKey = new ApiKey(keyHash, clientName);
        apiKeyRepository.save(apiKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "apiKey", rawKey,
                "mensagem", "Guarde esta chave em um local seguro. Ela não poderá ser exibida novamente."
        ));
    }

    public static String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao calcular hash da API Key", e);
        }
    }
}