package com.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {

        return new OpenAPI()

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
                                        - Tratamento global de erros.

                                        ### Relacionamentos

                                        A API utiliza relacionamentos JPA entre suas entidades:

                                        - Usuário e Endereço: One-to-One;
                                        - Usuário e Empréstimo: One-to-Many;
                                        - Livro e Autor: Many-to-Many;
                                        - Empréstimo e Livro: Many-to-One.

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