
package com.biblioteca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(
        name = "Termos de Uso",
        description = "Condições de uso da API. Acesso público."
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "405",
                description = "Método HTTP não permitido para esta rota",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(type = "object")
                )
        ),
        @ApiResponse(
                responseCode = "429",
                description = "Limite de requisições excedido",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(type = "object")
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Erro interno no servidor",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(type = "object")
                )
        )
})
public class TermosDeUsoController {

    public static final String VERSAO_TERMOS = "1.0";

    private static final String TEXTO = """
            TERMOS DE USO DA API DA BIBLIOTECA COMUNITÁRIA - versão 1.0

            1. Objeto. Esta API permite consultar e gerenciar o acervo, usuários, endereços e empréstimos da biblioteca.

            2. Acesso. Leituras de livros e autores são públicas. As demais operações exigem uma chave de API válida, enviada no cabeçalho X-API-Key. A chave é pessoal e intransferível.

            3. Níveis de acesso. ADMIN possui acesso completo à API, incluindo o gerenciamento de chaves. OPERADOR pode consultar e realizar operações nos recursos da biblioteca, mas não pode gerenciar chaves de API. Tentativas de acesso não permitido resultam em erro 403.

            4. Limites de uso. Cada IP pode fazer um número limitado de requisições por minuto. Ao exceder, a API responde 429 e informa, em Retry-After, quantos segundos aguardar.

            5. Responsabilidade. O cliente deve guardar sua chave em local seguro e comunicar imediatamente qualquer uso indevido, para que a chave seja desativada.

            6. Dados pessoais. Dados de usuários e empréstimos são usados apenas para o funcionamento do serviço da biblioteca.

            7. Disponibilidade. O serviço pode sofrer interrupções para manutenção, sem garantia de disponibilidade contínua.

            8. Alterações. Estes termos podem ser atualizados. A versão vigente é indicada no cabeçalho X-Termos-Versao.
            """;

    @GetMapping(
            value = "/termos-de-uso",
            produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"
    )
    @Operation(
            summary = "Termos de uso da API",
            description = "Retorna os termos de uso vigentes. Acesso público. A versão aparece no cabeçalho X-Termos-Versao."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Termos retornados com sucesso",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                    )
            )
    })
    public ResponseEntity<String> termosDeUso() {

        return ResponseEntity.ok()
                .header("X-Termos-Versao", VERSAO_TERMOS)
                .body(TEXTO);
    }
}
