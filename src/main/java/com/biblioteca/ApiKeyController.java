package com.biblioteca;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyController(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @PostMapping
    public ResponseEntity<ApiKey> gerarKey(@RequestParam String usuario) {
        String newKey = "bib-" + UUID.randomUUID().toString().replace("-", "");
        ApiKey apiKey = new ApiKey(newKey, usuario);
        ApiKey salva = apiKeyRepository.save(apiKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @GetMapping
    public ResponseEntity<List<ApiKey>> listarKeys() {
        return ResponseEntity.ok(apiKeyRepository.findAll());
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativarKey(@PathVariable Long id) {
        return apiKeyRepository.findById(id)
                .map(key -> {
                    key.setAtiva(false);
                    apiKeyRepository.save(key);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}