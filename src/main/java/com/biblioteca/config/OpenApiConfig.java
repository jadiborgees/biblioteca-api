package com.biblioteca.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1 - Padrão HATEOAS")
                .pathsToMatch("/**")
                .pathsToExclude("/api/keys/**")
                .addOpenApiCustomizer(openApi -> openApi.getInfo().setVersion("v1.0"))
                .build();
    }

    @Bean
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder()
                .group("v2 - Completa com recursos DTO")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(openApi -> openApi.getInfo().setVersion("v2.0"))
                .build();
    }

    /**
     * Ajustes aplicados a todas as operações:
     * - POST: cabeçalho X-Idempotency-Key e respostas 415 e 422.
     * - Rotas públicas (GET /livros e /autores): sem exigência de chave na documentação.
     * - Rotas protegidas: respostas 401 e 403. Todas: 429.
     */
    @Bean
    public OperationCustomizer ajustesDeOperacao() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }
            String base = Optional.ofNullable(handlerMethod.getBeanType().getAnnotation(RequestMapping.class))
                    .map(RequestMapping::value)
                    .filter(valores -> valores.length > 0)
                    .map(valores -> valores[0])
                    .orElse("");
            boolean publica = handlerMethod.hasMethodAnnotation(GetMapping.class)
                    && (base.equals("/livros") || base.equals("/autores") || base.equals("/termos-de-uso"));

            if (handlerMethod.hasMethodAnnotation(PostMapping.class)) {
                addResposta(operation, "415", "Content-Type diferente de application/json.");
                addResposta(operation, "422", "X-Idempotency-Key reutilizada com outro método, rota ou corpo.");
            }

            if (publica) {
                operation.setSecurity(new ArrayList<>()); // sem cadeado no Swagger
            } else {
                addResposta(operation, "401", "Cabeçalho X-API-Key ausente, inválido ou inativo.");
                addResposta(operation, "403", "A chave não tem permissão para esta operação (nível de acesso).");
            }
            addResposta(operation, "429", "Limite de requisições excedido. Consulte Retry-After e X-RateLimit-*.");
            return operation;
        };
    }

    private static void addResposta(Operation operation, String codigo, String descricao) {
        if (!operation.getResponses().containsKey(codigo)) {
            operation.getResponses().addApiResponse(codigo, new ApiResponse().description(descricao));
        }
    }

    @Bean
    public OpenAPI bibliotecaOpenAPI() {

        Contact contact = new Contact()
                .name("Jadi Pereira Borges")
                .email("jadiborgees@gmail.com")
                .url("https://github.com/jadiborgees");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        String description = """
                API REST em Java e Spring Boot para gerenciamento de uma biblioteca comunitária: acervo de livros e autores, usuários, endereços e empréstimos.

                ---

                ### Autenticação por chave de API

                Rotas protegidas exigem o cabeçalho `X-API-Key`. Sem chave, ou com chave inválida ou inativa, a resposta é `401`. Chave sem permissão para a operação recebe `403`.

                | Rota | Acesso |
                |---|---|
                | `GET /livros`, `GET /autores` (e subrotas) | Público |
                | Demais operações (`POST`, `PUT`, `PATCH`, `DELETE`, usuários, endereços, empréstimos) | Chave válida |
                | `/api/keys/**` (gestão de chaves) | Chave de nível `ADMIN` |

                Níveis de acesso:
                - `ADMIN`: acesso total, inclusive a gestão de chaves.
                - `OPERADOR`: leitura e escrita em todos os recursos, exceto a gestão de chaves.
                - `LEITOR`: somente leitura (`GET`) nos recursos protegidos.

                Chaves de desenvolvimento: `bib-chave-demo-12345` (ADMIN), `bib-operador-demo-12345` (OPERADOR) e `bib-leitor-demo-12345` (LEITOR). Novas chaves são geradas em `POST /api/keys`. A chave em texto puro é exibida apenas na criação; o servidor guarda somente o hash.

                ---

                ### Idempotência em operações POST

                Envie `X-Idempotency-Key` para que um reenvio acidental (por exemplo, após queda de conexão) não execute a operação de novo.

                - **Primeira requisição:** processada. Se concluída com sucesso (2xx), a resposta fica guardada por 24 horas.
                - **Mesma chave, mesmo método, rota e corpo:** a resposta original é devolvida, com `Idempotent-Replayed: true`. Nada é criado de novo.
                - **Mesma chave com método, rota ou corpo diferentes:** `422 Unprocessable Entity`.
                - **Mesma chave com a primeira requisição ainda em andamento:** `409 Conflict`.
                - **Requisição que falhou (4xx ou 5xx):** a chave não fica registrada, e o cliente pode tentar de novo.

                ---

                ### Versionamento da API

                A versão é escolhida pelo cabeçalho `X-API-Version` em `GET /livros`:

                - **Versão 1** (padrão, sem o cabeçalho, ou `1`): lista paginada no formato HATEOAS.
                - **Versão 2** (`2`): lista paginada no formato `LivroV2DTO`, com metadados da versão.

                Qualquer outro valor retorna `400`. As respostas trazem `X-API-Version` com a versão usada.

                ---

                ### Limite de requisições (rate limiting)

                Cada IP pode fazer 60 requisições por minuto (configurável). Ao exceder, a resposta é `429 Too Many Requests`, com `Retry-After` indicando quantos segundos aguardar. Todas as respostas trazem `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`.

                ---

                ### CORS

                Origens autorizadas (exatas): `http://localhost:3000`, `http://localhost:4200`, `http://localhost:8081` e `https://biblioteca-app.vercel.app`. Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`. Os cabeçalhos `Location`, `Retry-After`, `X-RateLimit-*`, `X-API-Version` e `Idempotent-Replayed` são expostos ao navegador.

                ---

                ### Termos de uso

                As condições de uso estão em `GET /termos-de-uso` (público), com a versão no cabeçalho `X-Termos-Versao`.

                ---

                ### Erros

                Erros seguem um formato único: `timestamp`, `status`, `erro`, `mensagem`, `caminho` e, em validações, `campos` (erro por campo).

                | Código | Quando ocorre |
                |---|---|
                | 200 OK | Consulta, atualização ou ação concluída. |
                | 201 Created | Recurso criado (também na reprodução idempotente). |
                | 204 No Content | Exclusão concluída. |
                | 400 Bad Request | Validação, JSON malformado, parâmetro inválido ou versão de API não suportada. |
                | 401 Unauthorized | `X-API-Key` ausente, inválida ou inativa. |
                | 403 Forbidden | Chave sem permissão para a operação. |
                | 404 Not Found | Recurso ou rota não encontrada. |
                | 405 Method Not Allowed | Método HTTP não permitido na rota. |
                | 409 Conflict | Regra de negócio ou integridade violada, ou requisição idempotente em andamento. |
                | 415 Unsupported Media Type | `Content-Type` diferente de `application/json`. |
                | 422 Unprocessable Entity | `X-Idempotency-Key` reutilizada com outro método, rota ou corpo. |
                | 429 Too Many Requests | Limite de requisições excedido (ver `Retry-After`). |
                | 500 Internal Server Error | Erro inesperado no servidor. |

                ---

                ### Ordem de cadastro

                Por causa das chaves estrangeiras:
                1. **Endereço** (`POST /enderecos`).
                2. **Usuário** (`POST /usuarios`), que pode trazer um endereço aninhado.
                3. **Autor** (`POST /autores`).
                4. **Livro** (`POST /livros`), com os `id` dos autores existentes.
                5. **Empréstimo** (`POST /emprestimos`), com os `id` de usuário e livro existentes.

                Para excluir, siga a ordem inversa.

                ---

                ### Regras de negócio

                - **Devolução:** `PATCH /emprestimos/{id}/devolver` altera o status para `DEVOLVIDO`. Devolver um empréstimo já finalizado retorna `409`.
                - **Empréstimo:** um livro não pode ter dois empréstimos ativos ao mesmo tempo (retorna `409`).
                - **Datas:** a data do empréstimo não pode ser futura, e a devolução prevista não pode ser anterior ao empréstimo.
                - **Ano de publicação:** não pode ser maior que o ano atual.
                """;

        List<Tag> tags = List.of(
                new Tag().name("Termos de Uso").description("Condições de uso da API (público)"),
                new Tag().name("Chaves de API").description("Geração e gerenciamento de chaves de acesso. Exige chave ADMIN."),
                new Tag().name("Endereços").description("Dados residenciais e localizações"),
                new Tag().name("Usuários").description("Contas de leitores vinculadas a endereços"),
                new Tag().name("Autores").description("Criadores das obras do acervo"),
                new Tag().name("Livros").description("Catálogo bibliográfico, paginação, versionamento e autoria"),
                new Tag().name("Empréstimos").description("Prazos, fluxos e devoluções de empréstimos")
        );

        return new OpenAPI()
                .info(new Info()
                        .title("Gestão para Biblioteca Comunitária API")
                        .version("v2.0")
                        .description(description)
                        .contact(contact)
                        .license(license)
                        .termsOfService("http://localhost:8080/termos-de-uso"))
                .servers(List.of(new Server().url("http://localhost:8080").description("Ambiente local")))
                .tags(tags)
                .addSecurityItem(new SecurityRequirement().addList("X-API-Key"))
                .components(new Components()
                        .addSecuritySchemes("X-API-Key", new SecurityScheme()
                                .name("X-API-Key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Chave de acesso à API. Desenvolvimento: bib-chave-demo-12345")));
    }
}
