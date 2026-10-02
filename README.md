# Biblioteca API

API REST desenvolvida para o gerenciamento de uma biblioteca comunitária.

O projeto permite cadastrar e gerenciar livros, autores, usuários, endereços e empréstimos. A aplicação foi desenvolvida utilizando Java e Spring Boot, com persistência de dados através do Spring Data JPA e banco de dados H2.

Além das operações CRUD, a API possui paginação, consultas personalizadas, validação de dados, relacionamentos entre entidades, HATEOAS, documentação com Swagger/OpenAPI, idempotência, autenticação por API Key, Rate Limiting, CORS, versionamento e tratamento global de erros.

---

## Funcionalidades

A API permite:

- Cadastrar, consultar, atualizar e excluir livros
- Cadastrar, consultar, atualizar e excluir autores
- Cadastrar, consultar, atualizar e excluir usuários
- Cadastrar, consultar, atualizar e excluir endereços
- Registrar e gerenciar empréstimos
- Registrar a devolução de livros
- Buscar livros por título
- Buscar autores por nome
- Buscar usuários por nome
- Buscar endereços por cidade
- Buscar empréstimos por status
- Listar registros de forma paginada
- Validar os dados recebidos pela API
- Navegar entre recursos através de links HATEOAS
- Evitar cadastros duplicados através de idempotência
- Proteger endpoints utilizando API Key
- Limitar a quantidade de requisições por cliente
- Controlar acesso entre origens através de CORS
- Utilizar diferentes versões da API através de header
- Tratar erros de forma global e padronizada
- Consultar e testar os endpoints através do Swagger

---

## Tecnologias utilizadas

- Java 21
- Spring Boot 4.0.8
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

Além das entidades principais, a aplicação utiliza entidades auxiliares para o gerenciamento de idempotência e API Keys.

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
| API Keys | `/api-keys` |

Os recursos principais possuem operações REST utilizando os métodos `GET`, `POST`, `PUT` e `DELETE`.

Para empréstimos, também está disponível a operação de devolução:

```text
PATCH /emprestimos/{id}/devolver
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

## Idempotência

As operações de cadastro utilizam o header:

```text
X-Idempotency-Key
```

A idempotência evita que uma mesma requisição `POST` crie recursos duplicados quando for enviada mais de uma vez com a mesma chave.

Exemplo:

```text
X-Idempotency-Key: livro-idempotencia-001
```

No primeiro envio, o recurso é criado normalmente e a API retorna:

```text
201 Created
```

Se a mesma requisição for enviada novamente utilizando a mesma chave de idempotência, a API retorna o recurso já criado sem gerar uma duplicação.

---

## API Key

A API utiliza API Keys para controlar o acesso aos endpoints protegidos.

A chave deve ser enviada através do header:

```text
X-API-Key
```

Uma nova chave pode ser gerada através do endpoint de gerenciamento de API Keys.

Exemplo:

```text
POST /api-keys
```

A API também permite desativar uma chave cadastrada.

Requisições realizadas sem uma API Key nos endpoints protegidos recebem:

```text
401 Unauthorized
```

Chaves inválidas ou inativas também são rejeitadas pela API.

---

## Rate Limiting

A API possui controle de quantidade de requisições para impedir um número excessivo de chamadas em um curto período.

Quando o limite é ultrapassado, a API retorna:

```text
429 Too Many Requests
```

A resposta também utiliza o header:

```text
Retry-After
```

Esse header informa quanto tempo o cliente deve aguardar antes de realizar novas requisições.

---

## CORS

A aplicação possui configuração de CORS para controlar requisições realizadas por aplicações externas.

A configuração permite que clientes autorizados realizem requisições HTTP para a API respeitando as regras definidas pela aplicação.

O funcionamento do CORS pode ser verificado através de uma requisição `OPTIONS`.

---

## Versionamento

A API possui versionamento através do header:

```text
X-API-Version
```

O endpoint de teste de versionamento dos livros é:

```text
GET /livros/versao
```

Exemplo utilizando a versão 1:

```text
X-API-Version: 1
```

Exemplo utilizando a versão 2:

```text
X-API-Version: 2
```

As duas versões podem apresentar comportamentos ou respostas diferentes sem a necessidade de alterar a URL principal do recurso.

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
- `401 Unauthorized` para problemas relacionados à API Key
- `404 Not Found` quando um recurso não é encontrado
- `429 Too Many Requests` quando o limite de requisições é excedido
- `500 Internal Server Error` para erros internos inesperados

As respostas apresentam mensagens que ajudam a identificar o problema ocorrido.

---

## HATEOAS

A API utiliza **Spring HATEOAS** para adicionar links de navegação às respostas e facilitar o acesso aos recursos relacionados.

Os recursos podem apresentar links como:

- `self` — acesso ao próprio recurso
- `atualizar` — atualização do recurso
- `excluir` — exclusão do recurso

Os empréstimos também possuem o link `devolver`, utilizado para registrar a devolução de um livro.

---

## Documentação da API

A API utiliza **Springdoc OpenAPI** para gerar a documentação dos endpoints.

Com a aplicação em execução, o Swagger UI pode ser acessado em:

```text
http://localhost:8080/swagger-ui/index.html
```

Pelo Swagger é possível visualizar e testar os endpoints, parâmetros e códigos de resposta.

A documentação também possui suporte à autenticação através de `X-API-Key`. O botão **Authorize** permite informar uma API Key ativa para testar os endpoints protegidos diretamente pelo Swagger.

---

## Testes com Postman

O projeto possui uma coleção do Postman contendo requisições utilizadas para testar os endpoints da API.

A coleção inclui testes das operações CRUD e das principais funcionalidades adicionais, como:

- Paginação
- Consultas personalizadas
- Idempotência
- API Key
- Rate Limiting
- CORS
- Versionamento
- Validação e códigos de resposta

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
JDBC URL: jdbc:h2:mem:biblioteca
Usuário: sa
Senha: deixar em branco
```

Como o banco de dados está em memória, os dados cadastrados durante a execução são apagados quando a aplicação é encerrada.

Isso também inclui as API Keys geradas durante a execução. Após reiniciar a aplicação, uma nova API Key deve ser criada para realizar novos testes.

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

## Autora

**Jadi Pereira Borges**

Projeto desenvolvido como atividade acadêmica para aplicação dos conceitos de desenvolvimento de APIs REST com Java e Spring Boot.