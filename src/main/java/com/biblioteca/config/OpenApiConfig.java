package com.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API")
                        .version("1.0")
                        .description("API REST desenvolvida para o gerenciamento de uma biblioteca comunitária.\n\n" +
                                "### Sobre a API\n" +
                                "A Biblioteca API permite administrar os principais recursos de uma biblioteca, incluindo livros, autores, usuários, endereços e empréstimos.\n\n" +
                                "### Funcionalidades\n" +
                                "- Cadastro, consulta, atualização e exclusão de registros;\n" +
                                "- Busca de livros por título;\n" +
                                "- Busca de autores por nome;\n" +
                                "- Busca de usuários por nome;\n" +
                                "- Busca de endereços por cidade;\n" +
                                "- Consulta de empréstimos por status;\n" +
                                "- Controle de empréstimos e devoluções;\n" +
                                "- Listagens paginadas;\n" +
                                "- Validação dos dados enviados à API;\n" +
                                "- Relacionamentos entre as entidades;\n" +
                                "- Navegação entre recursos utilizando HATEOAS;\n" +
                                "- Tratamento global de erros.\n\n" +
                                "### Relacionamentos\n" +
                                "A API utiliza relacionamentos JPA entre suas entidades:\n" +
                                "- **Usuário e Endereço**: One-to-One;\n" +
                                "- **Usuário e Empréstimo**: One-to-Many;\n" +
                                "- **Livro e Autor**: Many-to-Many;\n" +
                                "- **Empréstimo e Livro**: Many-to-One.\n\n" +
                                "### Tratamento de erros\n" +
                                "A API utiliza tratamento global de exceções para retornar mensagens claras e códigos HTTP adequados."));
    }
}