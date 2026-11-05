
package com.biblioteca.controller;

import com.biblioteca.assembler.EmprestimoModelAssembler;
import com.biblioteca.dto.EmprestimoRequestDTO;
import com.biblioteca.exception.*;
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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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

import java.util.List;

@RestController
@RequestMapping("/emprestimos")
@Tag(
        name = "Empréstimos",
        description = "Controle operacional de prazos, fluxos e rotas de devolução via PATCH"
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "405",
                description = "Método HTTP não permitido para esta rota",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErroResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Erro interno no servidor",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErroResponse.class)
                )
        )
})
public class EmprestimoController {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmprestimoModelAssembler assembler;

    @GetMapping
    @Operation(
            summary = "Listar empréstimos",
            description = "Recupera uma lista paginada de todos os empréstimos.",
            operationId = "listarEmprestimos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de empréstimos recuperada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de paginação inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> listar(
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true)
            PagedResourcesAssembler<Emprestimo> pagedResourcesAssembler) {

        Page<Emprestimo> page = emprestimoRepository.findAll(pageable);

        PagedModel<EntityModel<Emprestimo>> pagedModel =
                pagedResourcesAssembler.toModel(page, assembler);

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar empréstimos por status",
            description = "Consulta personalizada que filtra empréstimos pelo status (ATIVO, DEVOLVIDO, ATRASADO).",
            operationId = "buscarEmprestimosPorStatus"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status informado inválido ou parâmetros de paginação incorretos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<PagedModel<EntityModel<Emprestimo>>> buscarPorStatus(
            @Parameter(
                    description = "Status do empréstimo",
                    required = true,
                    example = "ATIVO"
            )
            @RequestParam StatusEmprestimo status,

            @ParameterObject Pageable pageable,

            @Parameter(hidden = true)
            PagedResourcesAssembler<Emprestimo> pagedResourcesAssembler) {

        Page<Emprestimo> page =
                emprestimoRepository.findByStatus(status, pageable);

        PagedModel<EntityModel<Emprestimo>> pagedModel =
                pagedResourcesAssembler.toModel(page, assembler);

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar empréstimo por ID",
            description = "Retorna os detalhes de um empréstimo específico.",
            operationId = "buscarEmprestimoPorId"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo encontrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido. O identificador deve ser positivo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public EntityModel<Emprestimo> buscarPorId(
            @Parameter(
                    description = "Identificador único do empréstimo",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNotFoundException(id));

        return assembler.toModel(emprestimo);
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar novo empréstimo",
            description = "Registra um novo empréstimo vinculando usuário e livro válidos, com suporte à idempotência (X-Idempotency-Key). Impede novo empréstimo caso o livro já esteja ativo.",
            operationId = "cadastrarEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Empréstimo cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos ou JSON malformado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário ou livro não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de regra de negócio: livro já emprestado, dados inconsistentes ou primeira requisição com a mesma chave de idempotência ainda em processamento",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Tipo de conteúdo não suportado. Utilize application/json.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Chave X-Idempotency-Key reutilizada com método, rota ou corpo diferentes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<EntityModel<Emprestimo>> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para cadastro do empréstimo",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Emprestimo.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "dataEmprestimo": "2026-10-30",
                                      "dataDevolucaoPrevista": "2026-11-13",
                                      "status": "ATIVO",
                                      "usuario": { "id": 1 },
                                      "livro": { "id": 1 }
                                    }
                                    """)
                    )
            )
            @Parameter(
                    description = "Chave única do cliente (1 a 255 caracteres: letras, números, '-', '_', ':' ou '.'). Repetir o envio com a mesma chave e o mesmo corpo devolve a resposta original.",
                    example = "cadastro-0001"
            )
            @RequestHeader(
                    value = "X-Idempotency-Key",
                    required = false
            )
            String idempotencyKey,

            @Valid @RequestBody EmprestimoRequestDTO dto) {

        Emprestimo emprestimo = dto.toEntity();

        if (emprestimo.getUsuario() == null
                || emprestimo.getUsuario().getId() == null) {

            throw new RegraNegocioException(
                    "O usuário é obrigatório e deve conter um ID válido."
            );
        }

        if (emprestimo.getLivro() == null
                || emprestimo.getLivro().getId() == null) {

            throw new RegraNegocioException(
                    "O livro é obrigatório e deve conter um ID válido."
            );
        }

        Usuario usuario = usuarioRepository.findById(
                emprestimo.getUsuario().getId()
        ).orElseThrow(() ->
                new UsuarioNotFoundException(
                        emprestimo.getUsuario().getId()
                )
        );

        Livro livro = livroRepository.findById(
                emprestimo.getLivro().getId()
        ).orElseThrow(() ->
                new LivroNotFoundException(
                        emprestimo.getLivro().getId()
                )
        );

        if (emprestimo.getDataDevolucaoPrevista() != null
                && emprestimo.getDataEmprestimo() != null
                && emprestimo.getDataDevolucaoPrevista()
                .isBefore(emprestimo.getDataEmprestimo())) {

            throw new RegraNegocioException(
                    "A data de devolução prevista não pode ser anterior à data de empréstimo."
            );
        }

        List<Emprestimo> emprestimosAtivos =
                emprestimoRepository.findByLivroIdAndStatusIn(
                        livro.getId(),
                        List.of(
                                StatusEmprestimo.ATIVO,
                                StatusEmprestimo.ATRASADO
                        )
                );

        if (!emprestimosAtivos.isEmpty()) {
            throw new RegraNegocioException(
                    "Este livro já está emprestado e ainda não foi devolvido."
            );
        }

        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);

        if (emprestimo.getStatus() == null) {
            emprestimo.setStatus(StatusEmprestimo.ATIVO);
        }

        Emprestimo novoEmprestimo =
                emprestimoRepository.save(emprestimo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(novoEmprestimo));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar empréstimo existente",
            description = "Atualiza os dados gerais de um empréstimo.",
            operationId = "atualizarEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID ou dados de atualização inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo, usuário ou livro não localizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de regra de negócio, como datas inconsistentes ou alteração que viole as regras do empréstimo",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Tipo de conteúdo não suportado. Utilize application/json.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public EntityModel<Emprestimo> atualizar(
            @Parameter(
                    description = "Identificador único do empréstimo",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id,

            @Valid @RequestBody EmprestimoRequestDTO dto) {

        Emprestimo emprestimo = dto.toEntity();

        Emprestimo existente = emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNotFoundException(id));

        if (emprestimo.getUsuario() != null
                && emprestimo.getUsuario().getId() != null) {

            Usuario usuario = usuarioRepository.findById(
                    emprestimo.getUsuario().getId()
            ).orElseThrow(() ->
                    new UsuarioNotFoundException(
                            emprestimo.getUsuario().getId()
                    )
            );

            existente.setUsuario(usuario);
        }

        if (emprestimo.getLivro() != null
                && emprestimo.getLivro().getId() != null) {

            Livro livro = livroRepository.findById(
                    emprestimo.getLivro().getId()
            ).orElseThrow(() ->
                    new LivroNotFoundException(
                            emprestimo.getLivro().getId()
                    )
            );

            existente.setLivro(livro);
        }

        if (emprestimo.getDataDevolucaoPrevista() != null
                && emprestimo.getDataEmprestimo() != null
                && emprestimo.getDataDevolucaoPrevista()
                .isBefore(emprestimo.getDataEmprestimo())) {

            throw new RegraNegocioException(
                    "A data de devolução prevista não pode ser anterior à data de empréstimo."
            );
        }

        existente.setDataEmprestimo(emprestimo.getDataEmprestimo());
        existente.setDataDevolucaoPrevista(
                emprestimo.getDataDevolucaoPrevista()
        );
        existente.setDataDevolucao(emprestimo.getDataDevolucao());

        if (emprestimo.getStatus() != null) {
            existente.setStatus(emprestimo.getStatus());
        }

        Emprestimo emprestimoAtualizado =
                emprestimoRepository.save(existente);

        return assembler.toModel(emprestimoAtualizado);
    }

    @PatchMapping("/{id}/devolver")
    @Operation(
            summary = "Devolver livro do empréstimo",
            description = "Registra a devolução do livro e atualiza o status para DEVOLVIDO. Empréstimos já devolvidos retornam 409 Conflict.",
            operationId = "devolverEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Devolução registrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido. O identificador deve ser positivo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito: empréstimo já se encontra devolvido e finalizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public EntityModel<Emprestimo> devolver(
            @Parameter(
                    description = "Identificador único do empréstimo a ser devolvido",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo existente = emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNotFoundException(id));

        if (existente.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new RegraNegocioException(
                    "Este empréstimo já foi devolvido e finalizado."
            );
        }

        existente.setStatus(StatusEmprestimo.DEVOLVIDO);
        existente = emprestimoRepository.save(existente);

        return assembler.toModel(existente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir empréstimo",
            description = "Remove um registro de empréstimo da base de dados.",
            operationId = "excluirEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Empréstimo excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido. O identificador deve ser positivo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public void excluir(
            @Parameter(
                    description = "Identificador único do empréstimo a ser excluído",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo existente = emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNotFoundException(id));

        emprestimoRepository.delete(existente);
    }
}
