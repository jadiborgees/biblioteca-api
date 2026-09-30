package com.biblioteca.controller;

import com.biblioteca.assembler.AutorModelAssembler;
import com.biblioteca.exception.AutorNotFoundException;
import com.biblioteca.model.Autor;
import com.biblioteca.repository.AutorRepository;

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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/autores")
@Tag(
        name = "Autores",
        description = "Endpoints para cadastro, consulta, atualização e exclusão dos autores da biblioteca"
)
public class AutorController {

    private final AutorRepository repository;
    private final AutorModelAssembler assembler;

    public AutorController(
            AutorRepository repository,
            AutorModelAssembler assembler) {

        this.repository = repository;
        this.assembler = assembler;
    }


    // LISTAR AUTORES
    @GetMapping
    @Operation(
            summary = "Listar autores",
            description = "Retorna todos os autores cadastrados de forma paginada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Autores listados com sucesso"
    )
    public PagedModel<EntityModel<Autor>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Autor> pagedAssembler) {

        Page<Autor> autores = repository.findAll(pageable);

        return pagedAssembler.toModel(autores, assembler);
    }


    // BUSCAR AUTORES POR NOME
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar autores por nome",
            description = "Busca autores que contenham o texto informado no nome. A busca não diferencia letras maiúsculas e minúsculas e o resultado é paginado."
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
    public PagedModel<EntityModel<Autor>> buscarPorNome(

            @Parameter(
                    description = "Nome completo ou parte do nome do autor",
                    example = "Machado"
            )
            @RequestParam String nome,

            @ParameterObject Pageable pageable,

            PagedResourcesAssembler<Autor> pagedAssembler) {

        Page<Autor> autores =
                repository.findByNomeContainingIgnoreCase(
                        nome,
                        pageable
                );

        return pagedAssembler.toModel(autores, assembler);
    }


    // BUSCAR AUTOR POR ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar autor por ID",
            description = "Retorna os dados de um autor específico a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autor encontrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor não encontrado"
            )
    })
    public EntityModel<Autor> buscarPorId(

            @Parameter(
                    description = "ID do autor",
                    example = "1"
            )
            @PathVariable Long id) {

        Autor autor = repository.findById(id)
                .orElseThrow(() ->
                        new AutorNotFoundException(id)
                );

        return assembler.toModel(autor);
    }


    // CADASTRAR AUTOR
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar autor",
            description = "Cadastra um novo autor na biblioteca."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do autor que será cadastrado",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "nome": "Machado de Assis"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Autor cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do autor inválidos"
            )
    })
    public EntityModel<Autor> cadastrar(
            @Valid @RequestBody Autor autor) {

        Autor novoAutor = repository.save(autor);

        return assembler.toModel(novoAutor);
    }


    // ATUALIZAR AUTOR
    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar autor",
            description = "Atualiza os dados de um autor já cadastrado na biblioteca."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do autor",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "nome": "Machado de Assis"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autor atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do autor inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor não encontrado"
            )
    })
    public EntityModel<Autor> atualizar(

            @Parameter(
                    description = "ID do autor que será atualizado",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid @RequestBody Autor autor) {

        Autor autorExistente = repository.findById(id)
                .orElseThrow(() ->
                        new AutorNotFoundException(id)
                );

        autorExistente.setNome(autor.getNome());

        Autor autorAtualizado =
                repository.save(autorExistente);

        return assembler.toModel(autorAtualizado);
    }


    // EXCLUIR AUTOR
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir autor",
            description = "Exclui permanentemente um autor cadastrado a partir do seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Autor excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor não encontrado"
            )
    })
    public void excluir(

            @Parameter(
                    description = "ID do autor que será excluído",
                    example = "1"
            )
            @PathVariable Long id) {

        Autor autor = repository.findById(id)
                .orElseThrow(() ->
                        new AutorNotFoundException(id)
                );

        repository.delete(autor);
    }
}