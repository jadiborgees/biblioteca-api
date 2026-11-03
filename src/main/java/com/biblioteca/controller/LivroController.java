package com.biblioteca.controller;

import com.biblioteca.assembler.LivroModelAssembler;
import com.biblioteca.dto.LivroV2DTO;
import com.biblioteca.exception.ErroResponse;
import com.biblioteca.exception.LivroNotFoundException;
import com.biblioteca.exception.RegraNegocioException;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.LivroRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/livros")
@Tag(name = "Livros", description = "Gerenciamento do acervo de livros da biblioteca")
@ApiResponses(value = {
        @ApiResponse(responseCode = "405", description = "Método HTTP não permitido para esta rota",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
})
public class LivroController {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private LivroModelAssembler assembler;

    // --- VERSIONAMENTO: Versão 1 (Padrão: quando X-API-Version não é informado ou é diferente de 2) ---
    @GetMapping(headers = "!X-API-Version=2")
    @Operation(summary = "Listar livros (v1)", description = "Recupera uma lista paginada de todos os livros do acervo no formato padrão HATEOAS (Versão 1).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de livros recuperada com sucesso",
                    headers = @Header(name = "X-API-Version", description = "Versão da API utilizada", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> listar(
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Livro> pagedResourcesAssembler) {

        Page<Livro> page = livroRepository.findAll(pageable);
        PagedModel<EntityModel<Livro>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok()
                .header("X-API-Version", "1.0")
                .body(pagedModel);
    }

    // --- VERSIONAMENTO: Versão 2 (Acionada quando X-API-Version=2 é enviado no cabeçalho) ---
    @GetMapping(headers = "X-API-Version=2")
    @Operation(summary = "Listar livros (v2)", description = "Recupera a lista de livros utilizando a estrutura estendida DTO (Versão 2).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de livros v2 recuperada com sucesso",
                    headers = @Header(name = "X-API-Version", description = "Versão da API utilizada", schema = @Schema(type = "string")))
    })
    public ResponseEntity<List<LivroV2DTO>> listarV2() {
        List<LivroV2DTO> livrosV2 = livroRepository.findAll()
                .stream()
                .map(livro -> new LivroV2DTO(livro, "v2.0 - Resposta estendida com metadados"))
                .toList();

        return ResponseEntity.ok()
                .header("X-API-Version", "2.0")
                .body(livrosV2);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar livros por título", description = "Consulta personalizada que busca livros contendo o termo informado no título.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca ou paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> buscarPorTitulo(
            @Parameter(description = "Termo ou título a ser pesquisado", required = true, example = "Dom")
            @RequestParam String titulo,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Livro> pagedResourcesAssembler) {

        Page<Livro> page = livroRepository.findByTituloContainingIgnoreCase(titulo, pageable);
        PagedModel<EntityModel<Livro>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por ID", description = "Retorna os detalhes de um livro específico pelo seu identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Livro localizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntityModel<Livro> buscarPorId(
            @Parameter(description = "Identificador único do livro", example = "1")
            @PathVariable Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));
        return assembler.toModel(livro);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo livro", description = "Cadastra um novo livro no acervo associando autores existentes com suporte a Idempotência (X-Idempotency-Key).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Livro cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou ISBN incorreto",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: Requisito de idempotência violado (payload divergente para a mesma chave X-Idempotency-Key) ou ano de publicação futuro/ISBN duplicado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "422", description = "Entidade Não Processável: Payload malformado ou dados inconsistentes para requisição idempotente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<EntityModel<Livro>> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do livro para cadastro",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Livro.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "titulo": "Dom Casmurro",
                                      "isbn": "9788535910663",
                                      "anoPublicacao": 1899,
                                      "autores": [
                                        { "id": 1 }
                                      ]
                                    }
                                    """)))
            @Valid @RequestBody Livro livro) {

        if (livro.getAnoPublicacao() != null && livro.getAnoPublicacao() > LocalDate.now().getYear()) {
            throw new RegraNegocioException("O ano de publicação não pode ser maior que o ano atual.");
        }

        Livro novoLivro = livroRepository.save(livro);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(novoLivro));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar livro existente", description = "Atualiza os dados de um livro cadastrado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Livro não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: ISBN informado pertence a outro livro ou ano de publicação futuro",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntityModel<Livro> atualizar(
            @Parameter(description = "Identificador único do livro", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Livro livro) {

        Livro existente = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        if (livro.getAnoPublicacao() != null && livro.getAnoPublicacao() > LocalDate.now().getYear()) {
            throw new RegraNegocioException("O ano de publicação não pode ser maior que o ano atual.");
        }

        existente.setTitulo(livro.getTitulo());
        existente.setIsbn(livro.getIsbn());
        existente.setAnoPublicacao(livro.getAnoPublicacao());
        if (livro.getAutores() != null && !livro.getAutores().isEmpty()) {
            existente.setAutores(livro.getAutores());
        }

        Livro livroAtualizado = livroRepository.save(existente);
        return assembler.toModel(livroAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir livro", description = "Remove um livro do acervo da biblioteca.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Livro excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade: não é possível excluir livro vinculado a empréstimos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public void excluir(
            @Parameter(description = "Identificador único do livro a ser excluído", example = "1")
            @PathVariable Long id) {
        Livro existente = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));
        livroRepository.delete(existente);
    }
}