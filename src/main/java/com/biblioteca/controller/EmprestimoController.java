package com.biblioteca.controller;

import com.biblioteca.assembler.EmprestimoModelAssembler;
import com.biblioteca.exception.EmprestimoNotFoundException;
import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import com.biblioteca.model.StatusEmprestimo;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.EmprestimoRepository;
import com.biblioteca.repository.LivroRepository;
import com.biblioteca.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/emprestimos")
@Tag(
        name = "Empréstimos",
        description = "Endpoints para cadastro, consulta, atualização, devolução e exclusão de empréstimos"
)
public class EmprestimoController {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoModelAssembler assembler;

    public EmprestimoController(
            EmprestimoRepository emprestimoRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            EmprestimoModelAssembler assembler) {

        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.assembler = assembler;
    }


    // LISTAR EMPRÉSTIMOS
    @GetMapping
    @Operation(
            summary = "Listar empréstimos",
            description = "Retorna todos os empréstimos cadastrados de forma paginada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Empréstimos listados com sucesso"
    )
    public PagedModel<EntityModel<Emprestimo>> listar(

            @Parameter(
                    description = "Número da página, iniciando em 0",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Quantidade de registros por página",
                    example = "2"
            )
            @RequestParam(defaultValue = "2") int size,

            PagedResourcesAssembler<Emprestimo> pagedAssembler) {

        Pageable pageable = PageRequest.of(page, size);

        return pagedAssembler.toModel(
                emprestimoRepository.findAll(pageable),
                assembler
        );
    }


    // BUSCAR EMPRÉSTIMOS POR STATUS
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar empréstimos por status",
            description = "Busca empréstimos pelo status informado. Os valores disponíveis são ATIVO, DEVOLVIDO e ATRASADO. O resultado é paginado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status ou parâmetros de paginação inválidos"
            )
    })
    public PagedModel<EntityModel<Emprestimo>> buscarPorStatus(

            @Parameter(
                    description = "Status do empréstimo",
                    example = "ATIVO"
            )
            @RequestParam StatusEmprestimo status,

            @Parameter(
                    description = "Número da página, iniciando em 0",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Quantidade de registros por página",
                    example = "2"
            )
            @RequestParam(defaultValue = "2") int size,

            PagedResourcesAssembler<Emprestimo> pagedAssembler) {

        Pageable pageable = PageRequest.of(page, size);

        return pagedAssembler.toModel(
                emprestimoRepository.findByStatus(status, pageable),
                assembler
        );
    }


    // BUSCAR EMPRÉSTIMO POR ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar empréstimo por ID",
            description = "Retorna os dados de um empréstimo específico a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo encontrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado"
            )
    })
    public EntityModel<Emprestimo> buscarPorId(

            @Parameter(
                    description = "ID do empréstimo",
                    example = "1"
            )
            @PathVariable Long id) {

        Emprestimo emprestimo = emprestimoRepository
                .findById(id)
                .orElseThrow(() ->
                        new EmprestimoNotFoundException(id)
                );

        return assembler.toModel(emprestimo);
    }


    // CADASTRAR EMPRÉSTIMO
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar empréstimo",
            description = "Cadastra um novo empréstimo relacionando um livro e um usuário previamente cadastrados."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do empréstimo que será cadastrado",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "dataEmprestimo": "2026-09-30",
                                      "dataDevolucao": null,
                                      "status": "ATIVO",
                                      "livro": {
                                        "id": 1
                                      },
                                      "usuario": {
                                        "id": 1
                                      }
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Empréstimo cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do empréstimo inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro ou usuário não encontrado"
            )
    })
    public EntityModel<Emprestimo> cadastrar(
            @Valid @RequestBody Emprestimo emprestimo) {

        Livro livro = livroRepository
                .findById(emprestimo.getLivro().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Livro não encontrado"
                        )
                );

        Usuario usuario = usuarioRepository
                .findById(emprestimo.getUsuario().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuário não encontrado"
                        )
                );

        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);

        Emprestimo novoEmprestimo =
                emprestimoRepository.save(emprestimo);

        return assembler.toModel(novoEmprestimo);
    }


    // ATUALIZAR EMPRÉSTIMO
    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar empréstimo",
            description = "Atualiza as datas, o status, o livro e o usuário relacionados a um empréstimo existente."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do empréstimo",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "dataEmprestimo": "2026-09-30",
                                      "dataDevolucao": "2026-10-07",
                                      "status": "DEVOLVIDO",
                                      "livro": {
                                        "id": 1
                                      },
                                      "usuario": {
                                        "id": 1
                                      }
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do empréstimo inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo, livro ou usuário não encontrado"
            )
    })
    public EntityModel<Emprestimo> atualizar(

            @Parameter(
                    description = "ID do empréstimo que será atualizado",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid @RequestBody Emprestimo emprestimo) {

        Emprestimo existente = emprestimoRepository
                .findById(id)
                .orElseThrow(() ->
                        new EmprestimoNotFoundException(id)
                );

        Livro livro = livroRepository
                .findById(emprestimo.getLivro().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Livro não encontrado"
                        )
                );

        Usuario usuario = usuarioRepository
                .findById(emprestimo.getUsuario().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuário não encontrado"
                        )
                );

        existente.setDataEmprestimo(emprestimo.getDataEmprestimo());
        existente.setDataDevolucao(emprestimo.getDataDevolucao());
        existente.setStatus(emprestimo.getStatus());
        existente.setLivro(livro);
        existente.setUsuario(usuario);

        Emprestimo emprestimoAtualizado =
                emprestimoRepository.save(existente);

        return assembler.toModel(emprestimoAtualizado);
    }


    // EXCLUIR EMPRÉSTIMO
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir empréstimo",
            description = "Exclui permanentemente um empréstimo cadastrado a partir do seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Empréstimo excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado"
            )
    })
    public void excluir(

            @Parameter(
                    description = "ID do empréstimo que será excluído",
                    example = "1"
            )
            @PathVariable Long id) {

        if (!emprestimoRepository.existsById(id)) {
            throw new EmprestimoNotFoundException(id);
        }

        emprestimoRepository.deleteById(id);
    }


    // REGISTRAR DEVOLUÇÃO
    @PatchMapping("/{id}/devolver")
    @Operation(
            summary = "Registrar devolução do empréstimo",
            description = "Altera o status do empréstimo para DEVOLVIDO e registra automaticamente a data atual como data de devolução."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Devolução registrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado"
            )
    })
    public EntityModel<Emprestimo> devolver(

            @Parameter(
                    description = "ID do empréstimo que será devolvido",
                    example = "1"
            )
            @PathVariable Long id) {

        Emprestimo emprestimo = emprestimoRepository
                .findById(id)
                .orElseThrow(() ->
                        new EmprestimoNotFoundException(id)
                );

        emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
        emprestimo.setDataDevolucao(java.time.LocalDate.now());

        Emprestimo emprestimoDevolvido =
                emprestimoRepository.save(emprestimo);

        return assembler.toModel(emprestimoDevolvido);
    }
}