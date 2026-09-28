package com.biblioteca.controller;

import com.biblioteca.assembler.LivroModelAssembler;
import com.biblioteca.exception.LivroNotFoundException;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.LivroRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/livros")
@Tag(
        name = "Livros",
        description = "Endpoints para gerenciamento dos livros da biblioteca"
)
public class LivroController {

    private final LivroRepository repository;
    private final LivroModelAssembler assembler;

    public LivroController(
            LivroRepository repository,
            LivroModelAssembler assembler) {

        this.repository = repository;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
            summary = "Listar livros",
            description = "Retorna os livros cadastrados de forma paginada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Livros listados com sucesso"
    )
    public PagedModel<EntityModel<Livro>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Livro> pagedAssembler) {

        Page<Livro> livros = repository.findAll(pageable);

        return pagedAssembler.toModel(livros, assembler);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar livro por ID",
            description = "Retorna um livro específico a partir do seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro encontrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado"
            )
    })
    public EntityModel<Livro> buscar(
            @Parameter(description = "ID do livro")
            @PathVariable Long id) {

        Livro livro = repository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        return assembler.toModel(livro);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar livro",
            description = "Cadastra um novo livro na biblioteca."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Livro cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do livro inválidos"
            )
    })
    public EntityModel<Livro> cadastrar(
            @Valid @RequestBody Livro livro) {

        Livro novoLivro = repository.save(livro);

        return assembler.toModel(novoLivro);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar livro",
            description = "Atualiza os dados de um livro existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do livro inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado"
            )
    })
    public EntityModel<Livro> editar(
            @Parameter(description = "ID do livro")
            @PathVariable Long id,
            @Valid @RequestBody Livro livroNovo) {

        Livro livro = repository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        livro.setTitulo(livroNovo.getTitulo());
        livro.setIsbn(livroNovo.getIsbn());
        livro.setAnoPublicacao(livroNovo.getAnoPublicacao());

        Livro livroAtualizado = repository.save(livro);

        return assembler.toModel(livroAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir livro",
            description = "Exclui um livro cadastrado a partir do seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Livro excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado"
            )
    })
    public void excluir(
            @Parameter(description = "ID do livro")
            @PathVariable Long id) {

        Livro livro = repository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        repository.delete(livro);
    }
}