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

Os empréstimos também possuem o link `devolver`, utilizado para registrar a devolução de um livro.

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
git clone [https://github.com/jadiborgees/biblioteca-api.git](https://github.com/jadiborgees/biblioteca-api.git)
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