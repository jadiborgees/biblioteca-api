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
API REST desenvolvida em Java e Spring Boot para o gerenciamento de um ecossistema de biblioteca comunitária. O sistema contempla o controle completo de acervo bibliográfico, autores, leitores, endereços e o ciclo de vida transacional de empréstimos.

---

### Arquitetura e Padrões de Projeto

- **Formato de Dados:** Todas as requisições e respostas operam estritamente em `application/json` codificado em UTF-8.

- **Persistência e Banco de Dados:** Utiliza Spring Data JPA com banco de dados em memória H2 (`jdbc:h2:mem:testdb`). Os dados inseridos em tempo de execução são redefinidos ao encerrar a aplicação.

- **Nível de Maturidade REST (HATEOAS):** As respostas incluem hipermídia dinâmica (`_links`) para facilitar a navegação entre recursos relacionados:

- `self`: Acesso direto ao recurso consultado.

- `livros` / `autores` / `usuarios` / `enderecos` / `emprestimos`: Links para coleções e entidades associadas.

- `devolver`: Operação específica vinculada ao fluxo de devolução de empréstimos ativos.


---

### Regras de Negócio e Automações

- **Validação de Prazos e Devolução:** O registro de devolução (`PATCH /emprestimos/{id}/devolver`) altera o status do empréstimo diretamente para `DEVOLVIDO`. Tentativas de devolver empréstimos que já se encontram finalizados retornam `409 Conflict`.

- **Campos e Datas:** A data do empréstimo aceita apenas valores presentes ou passados (`@PastOrPresent`). A data de devolução prevista pode ser informada opcionalmente no cadastro.


---

### Ordem Lógica de Cadastro (Pré-requisitos e Dependências)

Devido às restrições de chave estrangeira (`Foreign Key`) e integridade referencial do banco de dados, os cadastros via `POST` devem seguir uma ordem lógica estrita (FIFO):
1. **Endereço (`POST /enderecos`)**: Pode ser cadastrado previamente para reutilização por múltiplos usuários.

2. **Usuário (`POST /usuarios`)**: Aceita o vínculo com um endereço pré-existente (passando o `"id"`) ou com a criação de um novo endereço embutido no próprio payload.
3. **Autor (`POST /autores`)**: Pode ser cadastrado antes dos livros para associação no acervo.
4. **Livro (`POST /livros`)**: Pode receber uma lista com IDs de autores pré-existentes.
5. **Empréstimo (`POST /emprestimos`)**: Exige IDs válidos de um usuário e de um livro já cadastrados.

---

### Entidades e Relacionamentos

O modelo de domínio é composto por cinco entidades principais e um enumerador de controle:
- **Usuario e Endereco:** Relacionamento `One-to-One`.

- **Usuario e Emprestimo:** Relacionamento `One-to-Many`.

- **Livro e Autor:** Relacionamento `Many-to-Many` (livros podem ter múltiplos autores associados).

- **Emprestimo e Livro:** Relacionamento `Many-to-One`.

- **StatusEmprestimo (Enum):** Valores aceitos (`ATIVO`, `DEVOLVIDO`, `ATRASADO`).


---

### Paginação e Consultas Personalizadas

Para otimizar o tráfego de dados, as listagens gerais utilizam paginação padrão via `@ParameterObject Pageable`. Além disso, a API disponibiliza endpoints de busca especializada por parâmetros:
- **Livros:** Busca por título.

- **Autores:** Busca por nome.

- **Usuários:** Busca por nome.

- **Endereços:** Busca por cidade.

- **Empréstimos:** Busca por status.


---

### Validação de Dados e Tratamento de Erros

- **Bean Validation:** Validação declarativa rigorosa nos payloads de entrada (campos obrigatórios, formatação e restrições de tamanho). Dados inválidos retornam `400 Bad Request`.

- **Tratamento Global (`@ControllerAdvice`):** Centralização de exceções para padronizar mensagens descritivas de erro através da estrutura `ErroResponse`.


| Código HTTP | Significado e Cenário de Aplicação |
| :--- | :--- |
| **200 OK** | Sucesso na recuperação, atualização ou execução de comandos. |
| **201 Created** | Recurso criado com sucesso após envio de POST válido. |
| **204 No Content** | Sucesso na execução (geralmente em DELETE), sem conteúdo de retorno no corpo. |
| **400 Bad Request** | Erro de validação de dados ou requisição mal formatada. |
| **404 Not Found** | O recurso solicitado não foi localizado na base de dados. |
| **405 Method Not Allowed** | Método HTTP não permitido para a rota solicitada. |
| **409 Conflict** | Violação de regra de negócio ou restrição de integridade. |
| **500 Internal Server Error** | Erro inesperado no servidor ou falha interna. |

---

### Ordem Lógica de Teardown (Exclusão / LIFO)

Para testes manuais que respeitem as restrições de chave estrangeira ao apagar registos, elimine na ordem inversa da criação:
1. `DELETE /emprestimos/{id}`

2. `DELETE /livros/{id}`
3. `DELETE /autores/{id}`
4. `DELETE /usuarios/{id}`
5. `DELETE /enderecos/{id}`
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
                        .title("Gestão para Biblioteca Comunitária API")
                        .version("v1.0")
                        .description(description)
                        .contact(contact)
                        .license(license))
                .servers(List.of(devServer))
                .tags(tags);
    }
}