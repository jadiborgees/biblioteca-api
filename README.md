# Biblioteca API

API REST desenvolvida para o gerenciamento de uma biblioteca comunitária.

O projeto permite cadastrar e gerenciar livros, autores, usuários, endereços e empréstimos. A aplicação foi desenvolvida utilizando Java e Spring Boot, com persistência de dados através do Spring Data JPA e banco de dados H2.

Além das operações CRUD, a API possui paginação, consultas personalizadas, validação de dados, relacionamentos entre entidades, HATEOAS e documentação utilizando Swagger/OpenAPI.

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

- **Usuario e Endereco:** relacionamento One-to-One
- **Usuario e Emprestimo:** relacionamento One-to-Many
- **Livro e Autor:** relacionamento Many-to-Many
- **Emprestimo e Livro:** relacionamento Many-to-One

A entidade `Emprestimo` também utiliza o enum `StatusEmprestimo`, que possui os valores `ATIVO`, `DEVOLVIDO` e `ATRASADO`.

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

Os recursos possuem operações REST utilizando os métodos `GET`, `POST`, `PUT` e `DELETE`.

Para empréstimos, também está disponível a operação de devolução:

`PATCH /emprestimos/{id}/devolver`

---

## Paginação e consultas personalizadas

As listagens da API utilizam paginação para evitar o retorno de todos os registros de uma única vez.

Exemplo:

`GET /livros?page=0&size=2`

A API também possui consultas personalizadas para cada recurso:

- Livros por título
- Autores por nome
- Usuários por nome
- Endereços por cidade
- Empréstimos por status

---

## Documentação da API

A API utiliza **Springdoc OpenAPI** para gerar a documentação dos endpoints.

Com a aplicação em execução, a documentação pode ser acessada pelo Swagger UI:

`http://localhost:8080/swagger-ui`

Pelo Swagger é possível visualizar e testar os endpoints, parâmetros, exemplos de requisição e códigos de resposta da API.

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

---

## HATEOAS

A API utiliza **Spring HATEOAS** para adicionar links de navegação às respostas e facilitar o acesso aos recursos relacionados.

Os recursos podem apresentar links como:

- `self` — acesso ao próprio recurso
- `atualizar` — atualização do recurso
- `excluir` — exclusão do recurso

Os empréstimos também possuem o link `devolver`, utilizado para registrar a devolução de um livro.

---

## Autora

**Jadi Pereira Borges**

Projeto desenvolvido como atividade acadêmica para aplicação dos conceitos de desenvolvimento de APIs REST com Java e Spring Boot.