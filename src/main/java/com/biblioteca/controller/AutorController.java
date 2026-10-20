package com.biblioteca.controller;

import com.biblioteca.assembler.AutorModelAssembler;
import com.biblioteca.exception.AutorNotFoundException;
import com.biblioteca.exception.ErroResponse;
import com.biblioteca.model.Autor;
import com.biblioteca.repository.AutorRepository;
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

@RestController
@RequestMapping("/autores")
@Tag(name = "Autores", description = "Cadastro e gestão de criadores de obras literárias")
public class AutorController {

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private AutorModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Listar autores", description = "Recupera uma lista paginada de todos os autores cadastrados.", operationId = "listarAutores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de autores recuperada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Autor>>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Autor> pagedResourcesAssembler) {

        Page<Autor> page = autorRepository.findAll(pageable);
        PagedModel<EntityModel<Autor>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar autores por nome", description = "Consulta personalizada que filtra autores pelo nome (busca parcial e case-insensitive).", operationId = "buscarAutoresPorNome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca ou paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Autor>>> buscarPorNome(
            @Parameter(description = "Nome do autor para filtragem parcial", required = true, example = "Machado")
            @RequestParam String nome,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Autor> pagedResourcesAssembler) {

        Page<Autor> page = autorRepository.findByNomeContainingIgnoreCase(nome, pageable);
        PagedModel<EntityModel<Autor>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar autor por ID", description = "Retorna os detalhes de um autor específico.", operationId = "buscarAutorPorId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Autor não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 404,
                                      "erro": "Não Encontrado",
                                      "mensagem": "Autor não encontrado com o ID: 99",
                                      "caminho": "/autores/99"
                                    }
                                    """)))
    })
    public EntityModel<Autor> buscarPorId(
            @Parameter(description = "Identificador único do autor", example = "1")
            @PathVariable Long id) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new AutorNotFoundException(id));
        return assembler.toModel(autor);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo autor", description = "Registra um novo autor no sistema.", operationId = "cadastrarAutor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Autor cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<EntityModel<Autor>> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para cadastro do autor",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Autor.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "nome": "Machado de Assis",
                                      "nacionalidade": "Brasileira"
                                    }
                                    """)))
            @Valid @RequestBody Autor autor) {

        Autor novoAutor = autorRepository.save(autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(novoAutor));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar autor existente", description = "Atualiza os dados de um autor cadastrado.", operationId = "atualizarAutor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Autor não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntityModel<Autor> atualizar(
            @Parameter(description = "Identificador único do autor", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados para atualização do autor",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Autor.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "nome": "Joaquim Maria Machado de Assis",
                                      "nacionalidade": "Brasileira"
                                    }
                                    """)))
            @Valid @RequestBody Autor autor) {

        Autor existente = autorRepository.findById(id)
                .orElseThrow(() -> new AutorNotFoundException(id));

        existente.setNome(autor.getNome());
        existente.setNacionalidade(autor.getNacionalidade());

        Autor autorAtualizado = autorRepository.save(existente);
        return assembler.toModel(autorAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir autor", description = "Remove um autor do cadastro se não houver livros vinculados.", operationId = "excluirAutor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Autor excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Autor não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: autor possui livros vinculados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public void excluir(
            @Parameter(description = "Identificador único do autor a ser excluído", example = "1")
            @PathVariable Long id) {
        Autor existente = autorRepository.findById(id)
                .orElseThrow(() -> new AutorNotFoundException(id));
        autorRepository.delete(existente);
    }
}