package com.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API")
                        .version("1.0")
                        .description(
                                "API REST para gerenciamento de uma biblioteca comunitária. " +
                                        "Permite cadastrar e gerenciar livros, autores, usuários, " +
                                        "endereços e empréstimos. A API possui paginação, validação, " +
                                        "HATEOAS, idempotência e controle de acesso por API Key."
                        )
                        .contact(new Contact()
                                .name("Jadi Pereira Borges")));
    }
}