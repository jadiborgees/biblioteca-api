package com.biblioteca.controller;

import com.biblioteca.assembler.LivroModelAssembler;
import com.biblioteca.exception.ErroResponse;
import com.biblioteca.exception.LivroNotFoundException;
import com.biblioteca.exception.RegraNegocioException;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.LivroRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @GetMapping
    @Operation(summary = "Listar livros", description = "Recupera uma lista paginada de todos os livros do acervo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de livros recuperada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Livro> pagedResourcesAssembler) {

        Page<Livro> page = livroRepository.findAll(pageable);
        PagedModel<EntityModel<Livro>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por ID", description = "Retorna os detalhes de um livro específico pelo seu identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Livro localizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-30T10:00:00.000+00:00",
                                      "status": 404,
                                      "erro": "Não Encontrado",
                                      "mensagem": "Livro não encontrado com o ID: 99",
                                      "caminho": "/livros/99"
                                    }
                                    """)))
    })
    public EntityModel<Livro> buscarPorId(
            @Parameter(description = "Identificador único do livro", example = "1")
            @PathVariable Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));
        return assembler.toModel(livro);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo livro", description = "Cadastra um novo livro no acervo associando autores existentes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Livro cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos, ISBN incorreto ou ano de publicação futuro",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: ISBN já cadastrado no sistema",
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
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos ou ano de publicação futuro",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Livro não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: ISBN informado já pertence a outro livro",
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