package com.biblioteca.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String API_KEY = "X-API-Key";

    @Bean
    public OpenAPI bibliotecaOpenAPI() {

        return new OpenAPI()

                // Configuração da segurança por API Key
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        API_KEY,
                                        new SecurityScheme()
                                                .name("X-API-Key")
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.HEADER)
                                                .description(
                                                        "Informe uma API Key ativa para acessar os endpoints protegidos."
                                                )
                                )
                )

                // Aplica a API Key aos endpoints documentados
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(API_KEY)
                )

                // Informações gerais da API
                .info(
                        new Info()
                                .title("Biblioteca API")
                                .version("1.0")
                                .description("""
                                        API REST desenvolvida para o gerenciamento de uma biblioteca comunitária.

                                        ### Sobre a API

                                        A Biblioteca API permite administrar os principais recursos de uma biblioteca,
                                        incluindo livros, autores, usuários, endereços e empréstimos.

                                        ### Funcionalidades

                                        - Cadastro, consulta, atualização e exclusão de registros;
                                        - Busca de livros por título;
                                        - Busca de autores por nome;
                                        - Busca de usuários por nome;
                                        - Busca de endereços por cidade;
                                        - Consulta de empréstimos por status;
                                        - Controle de empréstimos e devoluções;
                                        - Listagens paginadas;
                                        - Validação dos dados enviados à API;
                                        - Relacionamentos entre as entidades;
                                        - Navegação entre recursos utilizando HATEOAS;
                                        - Idempotência nas operações de cadastro;
                                        - Proteção de endpoints utilizando API Key;
                                        - Rate Limiting;
                                        - Configuração de CORS;
                                        - Versionamento por header;
                                        - Tratamento global de erros.

                                        ### Relacionamentos

                                        A API utiliza relacionamentos JPA entre suas entidades:

                                        - Usuário e Endereço: One-to-One;
                                        - Usuário e Empréstimo: One-to-Many;
                                        - Livro e Autor: Many-to-Many;
                                        - Empréstimo e Livro: Many-to-One.

                                        ### Idempotência

                                        As operações de cadastro utilizam o header `X-Idempotency-Key`
                                        para evitar a criação duplicada de recursos quando uma mesma
                                        requisição é enviada novamente.

                                        ### Segurança

                                        A API possui gerenciamento de API Keys.

                                        O header `X-API-Key` é utilizado para controlar o acesso
                                        aos endpoints protegidos.

                                        Uma API Key pode ser gerada através dos endpoints de
                                        gerenciamento de API Keys.

                                        ### Rate Limiting

                                        A API limita a quantidade de requisições realizadas por cliente.

                                        Quando o limite é excedido, a API retorna o status
                                        `429 Too Many Requests` e informa o tempo de espera
                                        através do header `Retry-After`.

                                        ### CORS

                                        A API possui configuração de CORS para permitir
                                        requisições de aplicações autorizadas.

                                        ### Versionamento

                                        O versionamento da API pode ser realizado através
                                        do header `X-API-Version`.

                                        Exemplo:

                                        `X-API-Version: 1`

                                        ou

                                        `X-API-Version: 2`

                                        ### Tratamento de erros

                                        A API utiliza tratamento global de exceções para retornar
                                        mensagens claras e códigos HTTP adequados.

                                        ### Documentação

                                        Os endpoints são documentados utilizando Springdoc OpenAPI
                                        e podem ser consultados e testados através do Swagger UI.
                                        """)
                                .contact(
                                        new Contact()
                                                .name("Jadi Pereira Borges")
                                )
                                .license(
                                        new License()
                                                .name("Uso educacional")
                                )
                );
    }
}