
# Biblioteca API

API REST desenvolvida para o gerenciamento de uma biblioteca comunitária.

O projeto permite cadastrar e gerenciar livros, autores, usuários, endereços e empréstimos. A aplicação foi desenvolvida utilizando Java e Spring Boot, com persistência de dados através do Spring Data JPA e banco de dados H2.

Além das operações CRUD, a API possui paginação, consultas personalizadas, validação de dados, relacionamentos entre entidades, HATEOAS, tratamento global de erros e documentação com Swagger/OpenAPI.

---

## Funcionalidades

A API permite:

- Cadastrar, consultar, atualizar e excluir livros
- Cadastrar, consultar, atualizar e excluir autores
- Cadastrar, consultar, atualizar e excluir usuários
- Cadastrar, consultar, atualizar e excluir endereços
- Registrar e gerenciar empréstimos
- Registrar a devolução de livros
- Cancelar empréstimos ativos ou atrasados
- Buscar livros por título
- Buscar autores por nome
- Buscar usuários por nome
- Buscar endereços por cidade
- Buscar empréstimos por status
- Listar registros de forma paginada
- Validar os dados recebidos pela API
- Navegar entre recursos através de links HATEOAS
- Tratar erros de forma global e padronizada
- Consultar e testar os endpoints através do Swagger

---

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Data JPA
- Spring HATEOAS
- Bean Validation
- H2 Database
- Springdoc OpenAPI
- Swagger UI

---

## Entidades e relacionamentos

A API é composta por cinco entidades principais: **Livro, Autor, Usuario, Endereco e Emprestimo**.

Os relacionamentos entre elas são:

- **Usuario e Endereco:** One-to-One
- **Usuario e Emprestimo:** One-to-Many
- **Livro e Autor:** Many-to-Many
- **Emprestimo e Livro:** Many-to-One

A entidade `Emprestimo` utiliza o enum `StatusEmprestimo`, que possui os valores:

- `ATIVO`
- `DEVOLVIDO`
- `ATRASADO`
- `CANCELADO`

---

## Endpoints da API

A API disponibiliza operações de cadastro, consulta, atualização e exclusão para seus principais recursos.

| Recurso | Endpoint |
|---|---|
| Livros | `/livros` |
| Autores | `/autores` |
| Usuários | `/usuarios` |
| Endereços | `/enderecos` |
| Empréstimos | `/emprestimos` |

Os recursos principais possuem operações REST utilizando os métodos `GET`, `POST`, `PUT` e `DELETE`.

Para empréstimos, também estão disponíveis as operações de devolução e cancelamento:

```text
PATCH /emprestimos/{id}/devolver
PATCH /emprestimos/{id}/cancelar
```

---

## Paginação e consultas personalizadas

As listagens da API utilizam paginação para evitar o retorno de todos os registros de uma única vez.

Exemplo:

```text
GET /livros?page=0&size=2
```

A API também possui consultas personalizadas para cada recurso:

- Livros por título
- Autores por nome
- Usuários por nome
- Endereços por cidade
- Empréstimos por status

---

## Validação de dados

A API utiliza Bean Validation para validar os dados recebidos nas requisições.

Quando os dados enviados são inválidos, a API retorna:

```text
400 Bad Request
```

Exemplos de validação incluem campos obrigatórios, formato de e-mail e outras regras definidas nas entidades.

---

## Tratamento global de erros

A aplicação utiliza `@RestControllerAdvice` e `@ExceptionHandler` para centralizar o tratamento das exceções.

Entre os erros tratados estão:

- `400 Bad Request` para dados inválidos
- `404 Not Found` quando um recurso não é encontrado
- `500 Internal Server Error` para erros internos inesperados

As respostas apresentam mensagens que ajudam a identificar o problema ocorrido.

---

## HATEOAS

A API utiliza **Spring HATEOAS** para adicionar links de navegação às respostas e facilitar o acesso aos recursos relacionados.

Os recursos podem apresentar links como:

- `self` — acesso ao próprio recurso
- `atualizar` — atualização do recurso
- `excluir` — exclusão do recurso

Os empréstimos também possuem os links `devolver` e `cancelar`, utilizados para registrar a devolução ou o cancelamento de um empréstimo, conforme seu status.

---

## Documentação da API

A API utiliza **Springdoc OpenAPI** para gerar a documentação dos endpoints.

Com a aplicação em execução, o Swagger UI pode ser acessado em:

```text
http://localhost:8080/swagger-ui/index.html
```

Pelo Swagger é possível visualizar e testar os endpoints, parâmetros e códigos de resposta.

---

## Testes com Postman

O projeto possui uma coleção do Postman contendo requisições utilizadas para testar os endpoints da API.

A coleção inclui testes das operações CRUD e das principais funcionalidades, como paginação e consultas personalizadas.

O arquivo da coleção está disponível na pasta:

```text
postman/
```

---

## Como executar o projeto

### Pré-requisitos

- Java 21
- Maven
- Git

### Passos

Clone o repositório:

```bash
git clone https://github.com/jadiborgees/biblioteca-api.git
```

Acesse a pasta do projeto:

```bash
cd biblioteca-api
```

Execute a aplicação:

```bash
mvn spring-boot:run
```

A aplicação estará disponível em:

```text
http://localhost:8080
```

---

## Banco de dados

O projeto utiliza o **H2 Database** em memória para armazenamento dos dados durante a execução da aplicação.

O console do H2 pode ser acessado em:

```text
http://localhost:8080/h2-console
```

Configuração para acesso:

```text
JDBC URL: jdbc:h2:mem:testdb
Usuário: sa
Senha: deixar em branco
```

Como o banco de dados está em memória, os dados cadastrados durante a execução são apagados quando a aplicação é encerrada.

---

## Estrutura principal do projeto

```text
src/main/java/com/biblioteca
├── assembler
├── config
├── controller
├── exception
├── model
└── repository
```

- `assembler` — criação dos links HATEOAS
- `config` — configurações da API
- `controller` — endpoints REST
- `exception` — exceções e tratamento global de erros
- `model` — entidades e enum da aplicação
- `repository` — acesso aos dados utilizando Spring Data JPA

---

## Recursos avançados (Parte 2)

### Autenticação com chave de API

Rotas protegidas exigem o cabeçalho `X-API-Key`.

| Rota | Acesso |
|---|---|
| `GET /livros`, `GET /autores` (e subrotas) | Público |
| Demais operações (`POST`, `PUT`, `PATCH`, `DELETE`, usuários, endereços, empréstimos) | Chave válida |
| `/api/keys/**` | Chave `ADMIN` |

Níveis de acesso: `ADMIN` (tudo, inclusive chaves), `OPERADOR` (leitura e escrita, sem gestão de chaves) e `LEITOR` (somente `GET`). Sem chave ou com chave inválida: `401`. Sem permissão: `403`.

Gestão de chaves:

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/keys` | Gera uma chave. Corpo: `{"usuario": "...", "nivel": "OPERADOR"}`. A chave aparece só nesta resposta. |
| `GET` | `/api/keys` | Lista as chaves (versão mascarada). |
| `PATCH` | `/api/keys/{id}/ativar` | Reativa uma chave. |
| `PATCH` | `/api/keys/{id}/desativar` | Desativa uma chave. |
| `DELETE` | `/api/keys/{id}` | Exclui uma chave. |

O banco guarda somente o hash SHA-256 de cada chave.

Chaves de desenvolvimento (criadas na inicialização; não usar em produção):

| Chave | Nível |
|---|---|
| `bib-chave-demo-12345` | ADMIN |
| `bib-operador-demo-12345` | OPERADOR |
| `bib-leitor-demo-12345` | LEITOR |

```bash
curl -i http://localhost:8080/livros/1
# 200 OK (leitura pública)

curl -i -X POST http://localhost:8080/autores -H "X-API-Key: bib-leitor-demo-12345" \
  -H "Content-Type: application/json" -d '{"nome": "Clarice Lispector"}'
# 403 Forbidden (LEITOR não escreve)
```

### Idempotência em POST

Envie `X-Idempotency-Key` (1 a 255 caracteres: letras, números, `-`, `_`, `:` ou `.`).

| Situação | Resposta |
|---|---|
| Primeira requisição | Processada. Se concluída com 2xx, a resposta fica guardada por 24 horas. |
| Mesma chave, mesmo método, rota e corpo | Resposta original, com `Idempotent-Replayed: true`. Nada é criado de novo. |
| Mesma chave, outro método, rota ou corpo | `422 Unprocessable Entity` |
| Mesma chave com a primeira ainda em andamento | `409 Conflict` |
| Requisição que falhou (4xx ou 5xx) | Chave liberada para nova tentativa |

A chave é escopada ao cliente (hash da `X-API-Key`, ou IP quando não há chave).

### Rate limiting

O limite utiliza o algoritmo **Token Bucket** do Bucket4j, com capacidade de 30 tokens por IP e reposição gradual de 30 tokens ao longo de 60 segundos. Na prática, após o consumo dos tokens disponíveis, é reposto 1 token a cada 2 segundos. O balde permite rajadas de até 30 requisições quando está cheio.

Configuração em `application.properties`:

```properties
biblioteca.rate-limit.capacidade=30
biblioteca.rate-limit.periodo-segundos=60
```

Todas as respostas processadas pelo filtro trazem `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`. Ao exceder o limite, a API retorna `429 Too Many Requests` e o cabeçalho `Retry-After` informa quantos segundos aguardar até a próxima reposição de token.

### CORS

Origens exatas, em `biblioteca.cors.allowed-origins`: `http://localhost:3000`, `http://localhost:4200`, `http://localhost:8081` e `https://biblioteca-app.vercel.app`. O CORS roda antes da autenticação, então preflights não exigem `X-API-Key`.

### Versionamento

`GET /livros` aceita `X-API-Version`:

- `1` (padrão, ou sem o cabeçalho): lista paginada no formato HATEOAS.
- `2`: lista paginada no formato `LivroV2DTO`.

Qualquer outro valor retorna `400`.

```bash
curl -i http://localhost:8080/livros -H "X-API-Version: 2"
curl -i http://localhost:8080/livros -H "X-API-Version: 3"   # 400
```

### Validação e erros

- Os bodies de entrada usam DTOs de requisição com Bean Validation, o que impede o cliente de definir campos como `id`.
- Parâmetros de caminho e de consulta também são validados (ex.: `id` positivo, `titulo` obrigatório).
- Erros de validação retornam `400` com o mapa `campos`.
- Todos os erros seguem o mesmo formato, incluindo os produzidos pelos filtros (`401`, `403`, `429`).

### Requisições prontas

O arquivo `docs/requisicoes-parte2.http` tem as requisições de teste desta seção (IntelliJ ou VS Code com REST Client).

### Testes automatizados

`Parte2IntegrationTests` e `RateLimitIntegrationTests` cobrem autenticação, níveis, CORS, versionamento, idempotência, validação e rate limit. Execute com:

```bash
./mvnw test
```

---

## Códigos HTTP utilizados

| Código | Significado |
|---|---|
| 200 | OK — operação concluída. |
| 201 | Created — recurso criado. |
| 204 | No Content — exclusão concluída. |
| 400 | Bad Request — dados ou parâmetros inválidos. |
| 401 | Unauthorized — autenticação ausente ou inválida. |
| 403 | Forbidden — permissão insuficiente. |
| 404 | Not Found — recurso não encontrado. |
| 405 | Method Not Allowed — método não permitido. |
| 409 | Conflict — conflito de negócio, integridade ou idempotência em andamento. |
| 415 | Unsupported Media Type — tipo de conteúdo não suportado. |
| 422 | Unprocessable Entity — chave de idempotência reutilizada com dados diferentes. |
| 429 | Too Many Requests — limite de requisições excedido. |
| 500 | Internal Server Error — erro inesperado. |

## Autora

**Jadi Pereira Borges**

Projeto desenvolvido como atividade acadêmica para aplicação dos conceitos de desenvolvimento de APIs REST com Java e Spring Boot.
