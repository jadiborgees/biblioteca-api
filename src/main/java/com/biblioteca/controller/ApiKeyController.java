package com.biblioteca.controller;

import com.biblioteca.model.ApiKey;
import com.biblioteca.repository.ApiKeyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api-keys")
@Tag(
        name = "API Keys",
        description = "Endpoints para geração e gerenciamento das chaves de acesso à API"
)
public class ApiKeyController {

    private final ApiKeyRepository repository;

    public ApiKeyController(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Operation(
            summary = "Gerar uma API Key",
            description = "Gera uma nova chave que poderá ser utilizada no header X-API-Key."
    )
    public ResponseEntity<ApiKey> gerarApiKey() {

        String chave = UUID.randomUUID().toString();

        ApiKey apiKey = new ApiKey(chave, true);

        ApiKey novaApiKey = repository.save(apiKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(novaApiKey);
    }

    @GetMapping
    @Operation(
            summary = "Listar API Keys",
            description = "Lista as API Keys cadastradas."
    )
    public ResponseEntity<List<ApiKey>> listarApiKeys() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PatchMapping("/{id}/desativar")
    @Operation(
            summary = "Desativar uma API Key",
            description = "Desativa uma API Key para que ela não possa mais ser utilizada."
    )
    public ResponseEntity<ApiKey> desativarApiKey(@PathVariable Long id) {

        ApiKey apiKey = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("API Key não encontrada"));

        apiKey.setAtiva(false);

        return ResponseEntity.ok(repository.save(apiKey));
    }
}