package com.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

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
                # API de Biblioteca Direcionada - Documentação Técnica

                API REST desenvolvida em Java e Spring Boot para o gerenciamento de um ecossistema de biblioteca comunitária. O sistema contempla o controle completo de acervo bibliográfico, autores, leitores, endereços e o ciclo de vida transacional de empréstimos.

                ---

                ## Arquitetura e Padrões de Projeto

                - **Formato de Dados:** Todas as requisições e respostas operam estritamente em `application/json` codificado em UTF-8.
                - **Persistência e Banco de Dados:** Utiliza Spring Data JPA com banco de dados em memória H2 (`jdbc:h2:mem:biblioteca`). Os dados inseridos em tempo de execução são redefinidos ao encerrar a aplicação.
                - **Nível de Maturidade REST (HATEOAS):** As respostas incluem hipermídia dinâmica (`_links`) para facilitar a navegação entre recursos relacionados:
                  - `self`: Acesso direto ao recurso consultado.
                  - `atualizar`: Referência para modificação do registro.
                  - `excluir`: Referência para remoção do registro.
                  - `devolver`: Operação específica vinculada ao fluxo de empréstimos.

                ---

                ## Acesso à Consola H2 (Banco de Dados)

                Aceda a `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:biblioteca`, Utilizador: `sa`, Senha vazia).

                ---

                ## Entidades e Relacionamentos

                O modelo de domínio é composto por cinco entidades principais e um enumerador de controle:
                - **Usuario e Endereco:** Relacionamento `One-to-One` com propagação de ciclo de vida (`Cascade`).
                - **Usuario e Emprestimo:** Relacionamento `One-to-Many`.
                - **Livro e Autor:** Relacionamento `Many-to-One` (vários livros associados a um autor).
                - **Emprestimo e Livro:** Relacionamento `Many-to-One`.
                - **StatusEmprestimo (Enum):** Valores aceitos (`ATIVO`, `CONCLUIDO`, `ATRASADO`).

                ---

                ## Paginação e Consultas Personalizadas

                Para otimizar o tráfego de dados, as listagens gerais utilizam paginação (exemplo: `GET /livros?page=0&size=2`). Além disso, a API disponibiliza endpoints de busca especializada por parâmetros:
                - **Livros:** Busca por título.
                - **Autores:** Busca por nome.
                - **Usuários:** Busca por nome.
                - **Endereços:** Busca por cidade.
                - **Empréstimos:** Busca por status.

                ---

                ## Validação de Dados e Tratamento de Erros

                - **Bean Validation:** Validação declarativa rigorosa nos payloads de entrada (campos obrigatórios, formatação e restrições de tamanho). Dados inválidos retornam `400 Bad Request`.
                - **Tratamento Global (`@RestControllerAdvice`):** Centralização de exceções para padronizar mensagens descritivas de erro.

                | Código HTTP | Significado e Cenário de Aplicação |
                | :--- | :--- |
                | **200 OK** | Sucesso na recuperação, atualização ou execução de comandos. |
                | **201 Created** | Recurso criado com sucesso após envio de POST válido. |
                | **204 No Content** | Sucesso na execução (geralmente em DELETE), sem conteúdo de retorno no corpo. |
                | **400 Bad Request** | Erro de validação de dados ou requisição mal formatada. |
                | **404 Not Found** | O recurso solicitado não foi localizado na base de dados. |
                | **409 Conflict** | Violação de regra de negócio ou restrição de integridade. |
                | **500 Internal Server Error** | Erro inesperado no servidor ou falha interna. |

                ---

                ## Ordem Lógica de Operação e Teardown (Exclusão)

                Para testes manuais que respeitem a integridade referencial e restrições de chave estrangeira, elimine os registros na ordem inversa da criação (LIFO):
                1. `DELETE /emprestimos/{id}`
                2. `DELETE /livros/{id}`
                3. `DELETE /autores/{id}`
                4. `DELETE /usuarios/{id}`
                5. `DELETE /enderecos/{id}`

                ---

                ## Estrutura de Pacotes da Aplicação

                - `com.biblioteca.assembler` - Componentes de montagem e suporte HATEOAS.
                - `com.biblioteca.config` - Configurações da API, segurança e documentação OpenAPI.
                - `com.biblioteca.controller` - Controladores REST e mapeamento de rotas.
                - `com.biblioteca.exception` - Tratamento global de exceções e erros customizados.
                - `com.biblioteca.model` - Entidades de domínio e enumeradores.
                - `com.biblioteca.repository` - Interfaces de persistência com Spring Data JPA.
                """;

        List<Tag> tags = List.of(
                new Tag().name("Endereços").description("Gerenciamento de dados residenciais e localizações geográficas"),
                new Tag().name("Usuários").description("Contas de leitores vinculadas a endereços cadastrados"),
                new Tag().name("Autores").description("Cadastro e gestão de criadores de obras literárias"),
                new Tag().name("Livros").description("Catálogo bibliográfico, paginação e vínculos de autoria"),
                new Tag().name("Empréstimos").description("Controle operacional de prazos, fluxos e rotas de devolução via PATCH")
        );

        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Ambiente Local de Execução (Spring Boot)");

        return new OpenAPI()
                .info(new Info()
                        .title("API de Biblioteca Direcionada")
                        .version("v1.0")
                        .description(description)
                        .contact(contact)
                        .license(license))
                .servers(List.of(devServer))
                .tags(tags);
    }
}