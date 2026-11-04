package com.biblioteca.controller;

import org.springframework.web.bind.annotation.RequestHeader;

import com.biblioteca.dto.ApiKeyCriadaDTO;
import com.biblioteca.dto.ApiKeyRequestDTO;
import com.biblioteca.dto.ApiKeyResumoDTO;
import com.biblioteca.exception.ErroResponse;
import com.biblioteca.service.ApiKeyService;
import com.biblioteca.service.ApiKeyService.ChaveGerada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keys")
@Tag(name = "Chaves de API", description = "Geração e gerenciamento de chaves de API. Todas as operações exigem chave de nível ADMIN.")
@SecurityRequirement(name = "X-API-Key")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    @Operation(summary = "Gerar chave de API",
            description = "Gera uma chave para um usuário com o nível de acesso informado. A chave em texto puro é retornada somente nesta resposta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chave gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (usuário ausente ou nível inexistente)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Cabeçalho X-API-Key ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "A chave informada não é de nível ADMIN")
    })
    public ResponseEntity<ApiKeyCriadaDTO> gerar(@Parameter(description = "Chave única do cliente (1 a 255 caracteres: letras, números, '-', '_', ':' ou '.'). Repetir o envio com a mesma chave e o mesmo corpo devolve a resposta original.", example = "cadastro-0001")
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ApiKeyRequestDTO requisicao) {
        ChaveGerada gerada = apiKeyService.gerar(requisicao.usuario(), requisicao.nivel());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiKeyCriadaDTO.from(gerada.entidade(), gerada.chavePlana()));
    }

    @GetMapping
    @Operation(summary = "Listar chaves de API",
            description = "Lista as chaves cadastradas. Somente a versão mascarada de cada chave é exibida.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Cabeçalho X-API-Key ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "A chave informada não é de nível ADMIN")
    })
    public ResponseEntity<List<ApiKeyResumoDTO>> listar() {
        return ResponseEntity.ok(apiKeyService.listar().stream().map(ApiKeyResumoDTO::from).toList());
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar chave de API",
            description = "Desativa a chave. Requisições com ela passam a receber 401.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chave desativada"),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<ApiKeyResumoDTO> desativar(
            @Parameter(description = "Identificador da chave", example = "2") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiKeyResumoDTO.from(apiKeyService.alterarStatus(id, false)));
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Reativar chave de API", description = "Reativa uma chave desativada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chave reativada"),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<ApiKeyResumoDTO> ativar(
            @Parameter(description = "Identificador da chave", example = "2") @PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiKeyResumoDTO.from(apiKeyService.alterarStatus(id, true)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir chave de API", description = "Remove permanentemente a chave.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Chave excluída"),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public void excluir(@Parameter(description = "Identificador da chave", example = "2") @PathVariable @Positive Long id) {
        apiKeyService.excluir(id);
    }
}
