package com.biblioteca.controller;

import com.biblioteca.assembler.LivroModelAssembler;
import com.biblioteca.exception.LivroNotFoundException;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LivroRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;


@RestController
@RequestMapping("/livros")
@Tag(
        name = "Livros",
        description = "Endpoints para cadastro, consulta, atualização e exclusão dos livros da biblioteca"
)
public class LivroController {

    private final LivroRepository repository;
    private final AutorRepository autorRepository;
    private final LivroModelAssembler assembler;


    public LivroController(
            LivroRepository repository,
            AutorRepository autorRepository,
            LivroModelAssembler assembler) {

        this.repository = repository;
        this.autorRepository = autorRepository;
        this.assembler = assembler;
    }


    // LISTAR LIVROS
    @GetMapping
    @Operation(
            summary = "Listar livros",
            description = "Retorna todos os livros cadastrados de forma paginada."
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


    // BUSCAR LIVROS POR TÍTULO
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar livros por título",
            description = "Busca livros que contenham o texto informado no título. " +
                    "A busca não diferencia letras maiúsculas e minúsculas e o resultado é paginado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetro de busca inválido"
            )
    })
    public PagedModel<EntityModel<Livro>> buscarPorTitulo(

            @Parameter(
                    description = "Título completo ou parte do título do livro",
                    example = "Dom"
            )
            @RequestParam String titulo,

            @ParameterObject Pageable pageable,

            PagedResourcesAssembler<Livro> pagedAssembler) {

        Page<Livro> livros =
                repository.findByTituloContainingIgnoreCase(
                        titulo,
                        pageable
                );

        return pagedAssembler.toModel(livros, assembler);
    }


    // BUSCAR LIVRO POR ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar livro por ID",
            description = "Retorna os dados de um livro específico a partir do seu identificador."
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

            @Parameter(
                    description = "ID do livro",
                    example = "1"
            )
            @PathVariable Long id) {

        Livro livro = repository.findById(id)
                .orElseThrow(() ->
                        new LivroNotFoundException(id)
                );

        return assembler.toModel(livro);
    }


    // CADASTRAR LIVRO
    @PostMapping
    @Operation(
            summary = "Cadastrar livro",
            description = "Cadastra um novo livro na base de dados."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do livro que será cadastrado",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "titulo": "Quincas Borba",
                                      "isbn": "9788535910665",
                                      "anoPublicacao": 1891,
                                      "autores": []
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Livro cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do livro inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor informado não encontrado"
            )
    })
    public ResponseEntity<EntityModel<Livro>> cadastrar(
            @Valid @RequestBody Livro livro) {

        Set<Autor> autores = new HashSet<>();

        for (Autor autorRecebido : livro.getAutores()) {

            Autor autor = autorRepository
                    .findById(autorRecebido.getId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Autor não encontrado"
                            )
                    );

            autores.add(autor);
        }

        livro.setAutores(autores);

        // Cria o livro.
        Livro novoLivro = repository.save(livro);

        // Um novo recurso foi criado: 201 Created.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(novoLivro));
    }


    // ATUALIZAR LIVRO
    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar livro",
            description = "Atualiza título, ISBN, ano de publicação e autores de um livro existente."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do livro",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "titulo": "Quincas Borba - Edição Atualizada",
                                      "isbn": "9788535910665",
                                      "anoPublicacao": 1891,
                                      "autores": []
                                    }
                                    """
                    )
            )
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
                    description = "Livro ou autor não encontrado"
            )
    })
    public EntityModel<Livro> editar(

            @Parameter(
                    description = "ID do livro que será atualizado",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid @RequestBody Livro livroNovo) {

        Livro livro = repository.findById(id)
                .orElseThrow(() ->
                        new LivroNotFoundException(id)
                );

        Set<Autor> autores = new HashSet<>();

        for (Autor autorRecebido : livroNovo.getAutores()) {

            Autor autor = autorRepository
                    .findById(autorRecebido.getId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Autor não encontrado"
                            )
                    );

            autores.add(autor);
        }

        livro.setTitulo(livroNovo.getTitulo());
        livro.setIsbn(livroNovo.getIsbn());
        livro.setAnoPublicacao(livroNovo.getAnoPublicacao());
        livro.setAutores(autores);

        Livro livroAtualizado =
                repository.save(livro);

        return assembler.toModel(livroAtualizado);
    }


    // EXCLUIR LIVRO
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir livro",
            description = "Exclui permanentemente um livro cadastrado a partir do seu ID."
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

            @Parameter(
                    description = "ID do livro que será excluído",
                    example = "1"
            )
            @PathVariable Long id) {

        Livro livro = repository.findById(id)
                .orElseThrow(() ->
                        new LivroNotFoundException(id)
                );

        repository.delete(livro);
    }


    // VERSIONAMENTO DA API - VERSÃO 1
    @GetMapping(value = "/versao", headers = "X-API-Version=1")
    @Operation(
            summary = "Consultar versão 1 da API de livros",
            description = "Retorna a versão 1 do endpoint de livros utilizando o header X-API-Version."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Versão 1 retornada com sucesso"
    )
    public ResponseEntity<String> versao1() {

        return ResponseEntity.ok(
                "Biblioteca API - Livros - Versão 1"
        );
    }


    // VERSIONAMENTO DA API - VERSÃO 2
    @GetMapping(value = "/versao", headers = "X-API-Version=2")
    @Operation(
            summary = "Consultar versão 2 da API de livros",
            description = "Retorna a versão 2 do endpoint de livros utilizando o header X-API-Version."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Versão 2 retornada com sucesso"
    )
    public ResponseEntity<String> versao2() {

        return ResponseEntity.ok(
                "Biblioteca API - Livros - Versão 2"
        );
    }
}