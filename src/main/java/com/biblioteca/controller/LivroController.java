package com.biblioteca.controller;

import com.biblioteca.assembler.LivroModelAssembler;
import com.biblioteca.exception.*;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.AutorRepository;
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

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/livros")
@Tag(name = "Livros", description = "Catálogo bibliográfico, paginação e vínculos de autoria")
public class LivroController {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private LivroModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Listar livros", description = "Recupera uma lista paginada de livros.", operationId = "listarLivros")
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

    @GetMapping("/buscar")
    @Operation(summary = "Buscar livros por título", description = "Consulta personalizada que filtra livros pelo título (busca parcial e case-insensitive).", operationId = "buscarLivrosPorTitulo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca ou paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> buscarPorTitulo(
            @Parameter(description = "Título do livro para filtragem parcial", required = true, example = "Dom Casmurro")
            @RequestParam String titulo,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Livro> pagedResourcesAssembler) {

        Page<Livro> page = livroRepository.findByTituloContainingIgnoreCase(titulo, pageable);
        PagedModel<EntityModel<Livro>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por ID", description = "Retorna os detalhes de um livro específico.", operationId = "buscarLivroPorId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Livro encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 404,
                                      "erro": "Não Encontrado",
                                      "mensagem": "Livro não encontrado com o ID: 99",
                                      "caminho": "/livros/99"
                                    }
                                    """)))
    })
    public EntityModel<Livro> buscar(@PathVariable Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));
        return assembler.toModel(livro);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo livro", description = "Registra um novo livro no acervo. Exige ISBN único e vincula autores cadastrados.", operationId = "cadastrarLivro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Livro cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Autor não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: ISBN já cadastrado na base de dados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 409,
                                      "erro": "Conflito de Regra de Negócio",
                                      "mensagem": "Não foi possível concluir a operação pois o registro possui dependências vinculadas ou viola restrições de unicidade.",
                                      "caminho": "/livros"
                                    }
                                    """)))
    })
    public ResponseEntity<EntityModel<Livro>> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para cadastro de livro",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Livro.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "titulo": "Dom Casmurro",
                                      "isbn": "9788535902778",
                                      "anoPublicacao": 1899,
                                      "autores": [
                                        { "id": 1 }
                                      ]
                                    }
                                    """)))
            @Valid @RequestBody Livro livro) {

        if (livro.getAutores() != null && !livro.getAutores().isEmpty()) {
            Set<Autor> autoresCarregados = new HashSet<>();
            for (Autor a : livro.getAutores()) {
                if (a.getId() != null) {
                    Autor autorExistente = autorRepository.findById(a.getId())
                            .orElseThrow(() -> new AutorNotFoundException(a.getId()));
                    autoresCarregados.add(autorExistente);
                }
            }
            livro.setAutores(autoresCarregados);
        }

        Livro novoLivro = livroRepository.save(livro);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(novoLivro));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar livro existente", description = "Atualiza integralmente os dados de um livro.", operationId = "atualizarLivro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Livro ou Autor não localizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: ISBN já pertence a outro livro",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntityModel<Livro> atualizar(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados para atualização do livro",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Livro.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "titulo": "Dom Casmurro - Edição Especial",
                                      "isbn": "9788535902778",
                                      "anoPublicacao": 1900,
                                      "autores": [
                                        { "id": 1 }
                                      ]
                                    }
                                    """)))
            @Valid @RequestBody Livro livro) {

        Livro existente = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        existente.setTitulo(livro.getTitulo());
        existente.setIsbn(livro.getIsbn());
        existente.setAnoPublicacao(livro.getAnoPublicacao());

        if (livro.getAutores() != null) {
            Set<Autor> autoresCarregados = new HashSet<>();
            for (Autor a : livro.getAutores()) {
                if (a.getId() != null) {
                    Autor autorExistente = autorRepository.findById(a.getId())
                            .orElseThrow(() -> new AutorNotFoundException(a.getId()));
                    autoresCarregados.add(autorExistente);
                }
            }
            existente.setAutores(autoresCarregados);
        }

        Livro livroAtualizado = livroRepository.save(existente);
        return assembler.toModel(livroAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir livro", description = "Remove um livro da base de dados se não houver empréstimos associados.", operationId = "excluirLivro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Livro excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: livro possui empréstimos ativos ou histórico associado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public void excluir(@PathVariable Long id) {
        Livro existente = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));
        livroRepository.delete(existente);
    }
}