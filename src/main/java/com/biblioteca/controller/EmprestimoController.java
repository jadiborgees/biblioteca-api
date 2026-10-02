package com.biblioteca.controller;

import com.biblioteca.assembler.EmprestimoModelAssembler;
import com.biblioteca.exception.EmprestimoNotFoundException;
import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.IdempotencyKey;
import com.biblioteca.model.Livro;
import com.biblioteca.model.StatusEmprestimo;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.EmprestimoRepository;
import com.biblioteca.repository.IdempotencyKeyRepository;
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
import org.springframework.http.ResponseEntity;
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
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public EmprestimoController(
            EmprestimoRepository emprestimoRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            EmprestimoModelAssembler assembler,
            IdempotencyKeyRepository idempotencyKeyRepository) {

        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.assembler = assembler;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
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


    // CADASTRAR EMPRÉSTIMO COM IDEMPOTÊNCIA
    @PostMapping
    @Operation(
            summary = "Cadastrar empréstimo",
            description = "Cadastra um novo empréstimo relacionando um livro e um usuário previamente cadastrados. Utiliza X-Idempotency-Key para impedir o processamento duplicado da mesma operação."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do empréstimo que será cadastrado",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "dataEmprestimo": "2026-10-01",
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
                    responseCode = "200",
                    description = "Requisição já processada anteriormente. O empréstimo existente foi retornado"
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
    public ResponseEntity<EntityModel<Emprestimo>> cadastrar(

            @Parameter(
                    description = "Chave única utilizada para impedir o processamento duplicado da requisição",
                    example = "emprestimo-001",
                    required = true
            )
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,

            @Valid @RequestBody Emprestimo emprestimo) {

        // Prefixo para separar as chaves das outras entidades.
        String chaveInterna = "emprestimo:" + idempotencyKey;

        // Verifica se essa operação já foi processada.
        if (idempotencyKeyRepository.existsById(chaveInterna)) {

            IdempotencyKey chaveExistente =
                    idempotencyKeyRepository.findById(chaveInterna)
                            .orElseThrow();

            Long emprestimoId =
                    Long.valueOf(chaveExistente.getRecursoId());

            Emprestimo emprestimoExistente =
                    emprestimoRepository.findById(emprestimoId)
                            .orElseThrow(() ->
                                    new EmprestimoNotFoundException(emprestimoId)
                            );

            // Já existia: retorna o mesmo empréstimo com 200 OK.
            return ResponseEntity.ok(
                    assembler.toModel(emprestimoExistente)
            );
        }

        // Busca o livro informado.
        Livro livro = livroRepository
                .findById(emprestimo.getLivro().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Livro não encontrado"
                        )
                );

        // Busca o usuário informado.
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

        // Primeira requisição: cria o empréstimo.
        Emprestimo novoEmprestimo =
                emprestimoRepository.save(emprestimo);

        // Registra a chave e o ID do empréstimo criado.
        IdempotencyKey novaChave =
                new IdempotencyKey(
                        chaveInterna,
                        novoEmprestimo.getId().toString()
                );

        idempotencyKeyRepository.save(novaChave);

        // Novo recurso criado: 201 Created.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(novoEmprestimo));
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
                                      "dataEmprestimo": "2026-10-01",
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