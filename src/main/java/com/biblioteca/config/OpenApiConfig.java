
package com.biblioteca.config;

import com.biblioteca.exception.ErroResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SCHEMA_ERRO =
            "#/components/schemas/ErroResponse";

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1 - Padrão HATEOAS")
                .pathsToMatch("/**")
                .pathsToExclude("/api/keys/**")
                .addOpenApiCustomizer(openApi ->
                        openApi.getInfo().setVersion("v1.0"))
                .build();
    }

    @Bean
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder()
                .group("v2 - Completa com recursos DTO")
                .pathsToMatch("/**")
                .pathsToExclude("/api/keys/**")
                .addOpenApiCustomizer(openApi ->
                        openApi.getInfo().setVersion("v2.0"))
                .build();
    }

    @Bean
    public GroupedOpenApi apiKeysApi() {
        return GroupedOpenApi.builder()
                .group("Administração - Chaves de API")
                .pathsToMatch("/api/keys/**")
                .build();
    }

    @Bean
    public OperationCustomizer ajustesDeOperacao() {

        return (Operation operation, HandlerMethod handlerMethod) -> {

            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }

            String controller =
                    handlerMethod.getBeanType().getSimpleName();

            boolean get =
                    handlerMethod.hasMethodAnnotation(GetMapping.class);

            boolean post =
                    handlerMethod.hasMethodAnnotation(PostMapping.class);

            boolean put =
                    handlerMethod.hasMethodAnnotation(PutMapping.class);

            boolean patch =
                    handlerMethod.hasMethodAnnotation(PatchMapping.class);

            boolean delete =
                    handlerMethod.hasMethodAnnotation(DeleteMapping.class);

            boolean publica =
                    (controller.equals("AuthController") && post)
                            || (controller.equals("TermosDeUsoController") && get)
                            || ((controller.equals("LivroController")
                            || controller.equals("AutorController")) && get);

            boolean escrita = post || put || patch || delete;

            boolean recebeCorpo =
                    List.of(handlerMethod.getMethodParameters())
                            .stream()
                            .anyMatch(parametro ->
                                    parametro.hasParameterAnnotation(RequestBody.class));

            boolean utilizaIdempotencia =
                    post && !controller.equals("AuthController");

            /*
             * Rotas públicas não exigem X-API-Key.
             * As demais herdam a segurança global da API.
             */
            if (publica) {
                operation.setSecurity(new ArrayList<>());
            }

            /*
             * Erros comuns de requisição.
             */
            addRespostaErro(
                    operation,
                    "400",
                    "Requisição inválida, dados de entrada ou parâmetros incorretos."
            );

            addRespostaErro(
                    operation,
                    "405",
                    "Método HTTP não permitido para este recurso."
            );

            addRespostaErro(
                    operation,
                    "500",
                    "Erro interno inesperado no servidor."
            );

            addRespostaErro(
                    operation,
                    "429",
                    "Limite de requisições excedido. Consulte Retry-After e X-RateLimit-*."
            );

            /*
             * Autenticação.
             */
            if (publica && controller.equals("AuthController")) {

                addRespostaErro(
                        operation,
                        "401",
                        "Credenciais de acesso inválidas."
                );

            } else if (!publica) {

                addRespostaErro(
                        operation,
                        "401",
                        "Cabeçalho X-API-Key ausente, inválido ou inativo."
                );

                if (controller.equals("ApiKeyController")) {

                    addRespostaErro(
                            operation,
                            "403",
                            "Acesso negado. É necessária uma chave de nível ADMIN."
                    );

                } else {

                    addRespostaErro(
                            operation,
                            "403",
                            "A chave de API não possui permissão para executar esta operação."
                    );
                }
            }

            /*
             * Recursos que podem depender de IDs existentes.
             */
            boolean possuiParametroDeCaminho =
                    operation.getParameters() != null
                            && operation.getParameters().stream()
                            .anyMatch(parametro ->
                                    "path".equalsIgnoreCase(parametro.getIn()));

            if (possuiParametroDeCaminho) {

                addRespostaErro(
                        operation,
                        "404",
                        "Recurso ou referência relacionada não encontrado."
                );
            }

            /*
             * Conflitos de negócio, integridade ou idempotência.
             */
            if (escrita) {

                addRespostaErro(
                        operation,
                        "409",
                        "Conflito de regra de negócio, integridade de dados ou requisição idempotente ainda em processamento."
                );
            }

            /*
             * Resposta 415 somente quando existe @RequestBody.
             */
            if (recebeCorpo) {

                addRespostaErro(
                        operation,
                        "415",
                        "Tipo de conteúdo não suportado. Utilize application/json."
                );
            }

            /*
             * Resposta 422 nas operações POST com idempotência.
             */
            if (utilizaIdempotencia) {

                addRespostaErro(
                        operation,
                        "422",
                        "A X-Idempotency-Key foi reutilizada com outro método, rota ou corpo."
                );
            }

            /*
             * Garante que todas as respostas de erro existentes
             * também apresentem o schema ErroResponse.
             */
            for (var entrada : operation.getResponses().entrySet()) {

                String codigo = entrada.getKey();

                if (codigo.matches("4\\d\\d|5\\d\\d")) {
                    adicionarSchemaErro(entrada.getValue());
                }
            }

            return operation;
        };
    }

    private static void addRespostaErro(
            Operation operation,
            String codigo,
            String descricao) {

        ApiResponse resposta = operation.getResponses().get(codigo);

        if (resposta == null) {

            resposta = new ApiResponse()
                    .description(descricao);

            operation.getResponses().addApiResponse(
                    codigo,
                    resposta
            );

        } else if (resposta.getDescription() == null
                || resposta.getDescription().isBlank()) {

            resposta.setDescription(descricao);
        }

        adicionarSchemaErro(resposta);
    }

    private static void adicionarSchemaErro(ApiResponse resposta) {

        if (resposta.getContent() == null) {
            resposta.setContent(new Content());
        }

        MediaType json =
                resposta.getContent().get("application/json");

        if (json == null) {

            json = new MediaType();

            resposta.getContent().addMediaType(
                    "application/json",
                    json
            );
        }

        if (json.getSchema() == null) {

            json.setSchema(
                    new Schema<>().$ref(SCHEMA_ERRO)
            );
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

                Rotas protegidas exigem o cabeçalho `X-API-Key`.
                Sem chave, ou com chave inválida ou inativa, a resposta é `401`.
                Chave sem permissão para a operação recebe `403`.

                | Rota | Acesso |
                |---|---|
                | `POST /auth/login` | Público |
                | `GET /livros` e subrotas | Público |
                | `GET /autores` e subrotas | Público |
                | `GET /termos-de-uso` | Público |
                | Demais operações | Chave válida |
                | `/api/keys/**` | Chave de nível `ADMIN` |

                Níveis de acesso:
                - `ADMIN`: acesso total, inclusive à gestão de chaves.
                - `OPERADOR`: leitura e escrita nos recursos, exceto gestão de chaves.

                Novas chaves são geradas em `POST /api/keys`.
                A chave em texto puro é exibida apenas na criação; o servidor guarda somente o hash.

                ---

                ### Idempotência em operações POST

                Envie `X-Idempotency-Key` para evitar que um reenvio acidental execute a operação novamente.

                - **Primeira requisição:** processada. Se concluída com sucesso (2xx), a resposta fica guardada por 24 horas.
                - **Mesma chave, mesmo método, rota e corpo:** a resposta original é devolvida com `Idempotent-Replayed: true`.
                - **Mesma chave com método, rota ou corpo diferentes:** `422 Unprocessable Entity`.
                - **Mesma chave com a primeira requisição ainda em andamento:** `409 Conflict`.
                - **Requisição que falhou (4xx ou 5xx):** a chave não fica registrada, permitindo nova tentativa.

                ---

                ### Versionamento da API

                A versão é escolhida pelo cabeçalho `X-API-Version` em `GET /livros`:

                - **Versão 1:** padrão, sem o cabeçalho ou com valor `1`. Lista paginada no formato HATEOAS.
                - **Versão 2:** valor `2`. Lista paginada no formato `LivroV2DTO`, com metadados da versão.

                Qualquer outro valor retorna `400`.
                As respostas trazem `X-API-Version` com a versão utilizada.

                ---

                ### Limite de requisições

                O limite utiliza o algoritmo **Token Bucket** do Bucket4j, com capacidade de 30 tokens por IP e reposição gradual de 30 tokens ao longo de 60 segundos. Após consumir os tokens disponíveis, é reposto 1 token a cada 2 segundos. O balde permite uma rajada inicial de até 30 requisições quando está cheio.

                A configuração fica em `biblioteca.rate-limit.capacidade=30` e `biblioteca.rate-limit.periodo-segundos=60`. Ao exceder o limite, a resposta é `429 Too Many Requests`; o cabeçalho `Retry-After` informa quantos segundos aguardar até a próxima reposição de token. As respostas trazem `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`.

                ---

                ### CORS

                Origens autorizadas são configuradas em `biblioteca.cors.allowed-origins`.
                Incluem os ambientes locais, o domínio de produção do frontend e os domínios de preview configurados no Vercel.

                Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE` e `OPTIONS`.

                Os cabeçalhos `Location`, `Retry-After`, `X-RateLimit-*`, `X-API-Version` e `Idempotent-Replayed` são expostos ao navegador.

                ---

                ### Termos de uso

                As condições de uso estão em `GET /termos-de-uso` (público), com a versão no cabeçalho `X-Termos-Versao`.

                ---

                ### Formato das respostas de erro

                As respostas de erro utilizam o schema `ErroResponse`, com os campos:

                - `timestamp`: data e hora do erro.
                - `status`: código HTTP.
                - `erro`: identificação do tipo de erro.
                - `mensagem`: descrição do problema.
                - `caminho`: caminho da requisição.
                - `campos`: detalhes de validação por campo, quando aplicável.

                ---

                ### Ordem de cadastro

                Por causa das chaves estrangeiras:
                1. **Endereço** (`POST /enderecos`).
                2. **Usuário** (`POST /usuarios`), que pode trazer um endereço aninhado.
                3. **Autor** (`POST /autores`).
                4. **Livro** (`POST /livros`), com os IDs dos autores existentes.
                5. **Empréstimo** (`POST /emprestimos`), com os IDs de usuário e livro existentes.

                Para excluir, siga a ordem inversa.

                ---

                ### Regras de negócio

                - **Devolução:** `PATCH /emprestimos/{id}/devolver` altera o status para `DEVOLVIDO`, registra a data de devolução e restaura uma unidade ao estoque.
                - **Cancelamento:** `PATCH /emprestimos/{id}/cancelar` altera o status para `CANCELADO` e restaura uma unidade ao estoque.
                - **Empréstimo:** o mesmo usuário não pode possuir mais de um empréstimo `ATIVO` ou `ATRASADO` do mesmo livro. Usuários diferentes podem emprestar o mesmo título enquanto houver exemplares disponíveis no estoque.
                - **Estados:** os status possíveis são `ATIVO`, `ATRASADO`, `DEVOLVIDO` e `CANCELADO`. Devolver ou cancelar um empréstimo que não esteja `ATIVO` ou `ATRASADO` retorna `409 Conflict`.
                - **Datas:** a data do empréstimo não pode ser futura, e a devolução prevista não pode ser anterior ao empréstimo.
                - **Ano de publicação:** não pode ser maior que o ano atual.
                """;

        List<Tag> tags = List.of(
                new Tag().name("Termos de Uso")
                        .description("Condições de uso da API (público)"),
                new Tag().name("Autenticação")
                        .description("Login e autenticação de usuários"),
                new Tag().name("Chaves de API")
                        .description("Geração e gerenciamento de chaves de acesso. Exige chave ADMIN."),
                new Tag().name("Endereços")
                        .description("Dados residenciais e localizações"),
                new Tag().name("Usuários")
                        .description("Contas de leitores vinculadas a endereços"),
                new Tag().name("Autores")
                        .description("Criadores das obras do acervo"),
                new Tag().name("Livros")
                        .description("Catálogo bibliográfico, paginação, versionamento e autoria"),
                new Tag().name("Empréstimos")
                        .description("Prazos, fluxos e devoluções de empréstimos")
        );

        Components componentes = new Components()
                .addSecuritySchemes("X-API-Key", new SecurityScheme()
                        .name("X-API-Key")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("Chave de acesso à API enviada no cabeçalho X-API-Key."));

        /*
         * Registra automaticamente o modelo ErroResponse
         * como schema reutilizável no Swagger.
         */
        ModelConverters.getInstance()
                .read(ErroResponse.class)
                .forEach((nome, schema) ->
                        componentes.addSchemas(nome, schema));

        return new OpenAPI()
                .info(new Info()
                        .title("Gestão para Biblioteca Comunitária API")
                        .version("v2.0")
                        .description(description)
                        .contact(contact)
                        .license(license)
                        .termsOfService("https://biblioteca-api-10lf.onrender.com/termos-de-uso"))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Servidor atual (local ou produção)")
                ))
                .tags(tags)
                .addSecurityItem(new SecurityRequirement().addList("X-API-Key"))
                .components(componentes);
    }
}
