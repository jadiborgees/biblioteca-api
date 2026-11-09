
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/emprestimos")
@Tag(
        name = "Empréstimos",
        description = "Controle operacional de empréstimos, prazos, devoluções e cancelamentos."
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


    // =====================================================
    // LISTAR EMPRÉSTIMOS
    // =====================================================

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

        Page<Emprestimo> page =
                emprestimoRepository.findAll(pageable);

        PagedModel<EntityModel<Emprestimo>> pagedModel =
                pagedResourcesAssembler.toModel(page, assembler);

        return ResponseEntity.ok(pagedModel);
    }


    // =====================================================
    // BUSCAR POR STATUS
    // =====================================================

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar empréstimos por status",
            description = "Consulta empréstimos por status: ATIVO, DEVOLVIDO, ATRASADO ou CANCELADO.",
            operationId = "buscarEmprestimosPorStatus"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status informado inválido ou parâmetros incorretos",
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


    // =====================================================
    // BUSCAR EMPRÉSTIMO POR ID
    // =====================================================

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
                    description = "ID inválido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não localizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public EntityModel<Emprestimo> buscarPorId(
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo emprestimo =
                emprestimoRepository.findById(id)
                        .orElseThrow(() ->
                                new EmprestimoNotFoundException(id)
                        );

        return assembler.toModel(emprestimo);
    }


    // =====================================================
    // CADASTRAR EMPRÉSTIMO
    // =====================================================

    @PostMapping
    @Operation(
            summary = "Cadastrar novo empréstimo",
            description = "Registra um empréstimo quando existem exemplares disponíveis. Cada empréstimo reserva uma unidade do estoque.",
            operationId = "cadastrarEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Empréstimo cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário ou livro não localizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de regra de negócio ou estoque indisponível",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @Transactional
    public ResponseEntity<EntityModel<Emprestimo>> cadastrar(

            @Parameter(
                    description = "Chave única opcional da requisição.",
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

        // Busca o usuário.
        Usuario usuario = usuarioRepository.findById(
                emprestimo.getUsuario().getId()
        ).orElseThrow(() ->
                new UsuarioNotFoundException(
                        emprestimo.getUsuario().getId()
                )
        );

        // Bloqueia o livro durante a operação.
        Livro livro = livroRepository.findByIdComBloqueio(
                emprestimo.getLivro().getId()
        ).orElseThrow(() ->
                new LivroNotFoundException(
                        emprestimo.getLivro().getId()
                )
        );

        // Valida as datas.
        if (emprestimo.getDataDevolucaoPrevista() != null
                && emprestimo.getDataEmprestimo() != null
                && emprestimo.getDataDevolucaoPrevista()
                .isBefore(emprestimo.getDataEmprestimo())) {

            throw new RegraNegocioException(
                    "A data de devolução prevista não pode ser anterior à data de empréstimo."
            );
        }

        // Verifica o estoque disponível.
        if (livro.getQuantidadeDisponivel() <= 0) {

            throw new RegraNegocioException(
                    "Não há exemplares disponíveis deste livro para empréstimo."
            );
        }

        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);

        // Todo novo empréstimo começa como ATIVO.
        if (emprestimo.getStatus() == null) {

            emprestimo.setStatus(StatusEmprestimo.ATIVO);

        } else if (emprestimo.getStatus() != StatusEmprestimo.ATIVO) {

            throw new RegraNegocioException(
                    "Um novo empréstimo deve ser cadastrado com status ATIVO."
            );
        }

        // Desconta uma unidade do estoque.
        livro.setQuantidadeDisponivel(
                livro.getQuantidadeDisponivel() - 1
        );

        livroRepository.save(livro);

        // Salva o empréstimo na mesma transação.
        Emprestimo novoEmprestimo =
                emprestimoRepository.save(emprestimo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(novoEmprestimo));
    }


    // =====================================================
    // ATUALIZAR EMPRÉSTIMO
    // =====================================================

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar empréstimo existente",
            description = "Atualiza os dados gerais de um empréstimo sem permitir alteração direta do status ou troca do livro.",
            operationId = "atualizarEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID ou dados inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo ou usuário não localizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de regra de negócio",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @Transactional
    public EntityModel<Emprestimo> atualizar(

            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id,

            @Valid @RequestBody EmprestimoRequestDTO dto) {

        Emprestimo emprestimo = dto.toEntity();

        // Bloqueia o empréstimo durante a atualização.
        Emprestimo existente =
                emprestimoRepository.findByIdComBloqueio(id)
                        .orElseThrow(() ->
                                new EmprestimoNotFoundException(id)
                        );

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

        // Não permite alterar o status diretamente.
        if (emprestimo.getStatus() != null
                && emprestimo.getStatus() != existente.getStatus()) {

            throw new RegraNegocioException(
                    "A situação do empréstimo deve ser alterada pelas rotas específicas de devolução ou cancelamento."
            );
        }

        // Não permite trocar o livro.
        if (emprestimo.getLivro() != null
                && emprestimo.getLivro().getId() != null
                && !emprestimo.getLivro().getId()
                .equals(existente.getLivro().getId())) {

            throw new RegraNegocioException(
                    "Não é permitido trocar o livro de um empréstimo já cadastrado."
            );
        }

        if (emprestimo.getDataDevolucaoPrevista() != null
                && emprestimo.getDataEmprestimo() != null
                && emprestimo.getDataDevolucaoPrevista()
                .isBefore(emprestimo.getDataEmprestimo())) {

            throw new RegraNegocioException(
                    "A data de devolução prevista não pode ser anterior à data de empréstimo."
            );
        }

        existente.setDataEmprestimo(
                emprestimo.getDataEmprestimo()
        );

        existente.setDataDevolucaoPrevista(
                emprestimo.getDataDevolucaoPrevista()
        );

        existente.setDataDevolucao(
                emprestimo.getDataDevolucao()
        );

        Emprestimo emprestimoAtualizado =
                emprestimoRepository.save(existente);

        return assembler.toModel(emprestimoAtualizado);
    }


    // =====================================================
    // DEVOLVER EMPRÉSTIMO
    // =====================================================

    @PatchMapping("/{id}/devolver")
    @Operation(
            summary = "Devolver livro do empréstimo",
            description = "Registra a devolução e restaura uma unidade ao estoque. Não permite devolução duplicada.",
            operationId = "devolverEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Devolução registrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não localizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Empréstimo já finalizado ou não pode ser devolvido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @Transactional
    public EntityModel<Emprestimo> devolver(

            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        // Bloqueia o empréstimo para impedir duas devoluções simultâneas.
        Emprestimo existente =
                emprestimoRepository.findByIdComBloqueio(id)
                        .orElseThrow(() ->
                                new EmprestimoNotFoundException(id)
                        );

        if (existente.getStatus() == StatusEmprestimo.DEVOLVIDO) {

            throw new RegraNegocioException(
                    "Este empréstimo já foi devolvido e finalizado."
            );
        }

        if (existente.getStatus() != StatusEmprestimo.ATIVO
                && existente.getStatus() != StatusEmprestimo.ATRASADO) {

            throw new RegraNegocioException(
                    "Somente empréstimos ativos ou atrasados podem ser devolvidos."
            );
        }

        // Restaura exatamente uma unidade.
        restaurarEstoque(
                existente.getLivro().getId()
        );

        existente.setDataDevolucao(LocalDate.now());
        existente.setStatus(StatusEmprestimo.DEVOLVIDO);

        Emprestimo atualizado =
                emprestimoRepository.save(existente);

        return assembler.toModel(atualizado);
    }


    // =====================================================
    // CANCELAR EMPRÉSTIMO
    // =====================================================

    @PatchMapping("/{id}/cancelar")
    @Transactional
    @Operation(
            summary = "Cancelar empréstimo",
            description = "Cancela um empréstimo ativo ou atrasado e restaura uma unidade ao estoque. Não permite cancelamento duplicado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Empréstimo cancelado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não localizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Empréstimo já finalizado ou cancelado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public EntityModel<Emprestimo> cancelar(

            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo existente =
                emprestimoRepository.findByIdComBloqueio(id)
                        .orElseThrow(() ->
                                new EmprestimoNotFoundException(id)
                        );

        if (existente.getStatus() == StatusEmprestimo.CANCELADO) {

            throw new RegraNegocioException(
                    "Este empréstimo já foi cancelado."
            );
        }

        if (existente.getStatus() != StatusEmprestimo.ATIVO
                && existente.getStatus() != StatusEmprestimo.ATRASADO) {

            throw new RegraNegocioException(
                    "Somente empréstimos ativos ou atrasados podem ser cancelados."
            );
        }

        // Restaura exatamente uma unidade.
        restaurarEstoque(
                existente.getLivro().getId()
        );

        existente.setStatus(StatusEmprestimo.CANCELADO);

        Emprestimo cancelado =
                emprestimoRepository.save(existente);

        return assembler.toModel(cancelado);
    }


    // =====================================================
    // EXCLUIR EMPRÉSTIMO
    // =====================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir empréstimo",
            description = "Remove um registro. Se estiver ativo ou atrasado, restaura uma unidade ao estoque.",
            operationId = "excluirEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Empréstimo excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não localizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    @Transactional
    public void excluir(

            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo existente =
                emprestimoRepository.findByIdComBloqueio(id)
                        .orElseThrow(() ->
                                new EmprestimoNotFoundException(id)
                        );

        // Só restaura estoque se o empréstimo ainda ocupava exemplar.
        if (existente.getStatus() == StatusEmprestimo.ATIVO
                || existente.getStatus() == StatusEmprestimo.ATRASADO) {

            restaurarEstoque(
                    existente.getLivro().getId()
            );
        }

        emprestimoRepository.delete(existente);
    }


    // =====================================================
    // RESTAURAR ESTOQUE
    // =====================================================

    private void restaurarEstoque(Long livroId) {

        // Bloqueia o livro durante a atualização do estoque.
        Livro livro = livroRepository.findByIdComBloqueio(livroId)
                .orElseThrow(() ->
                        new LivroNotFoundException(livroId)
                );

        int total = livro.getQuantidadeTotal();
        int disponivel = livro.getQuantidadeDisponivel();

        // Proteção contra estoque acima do total cadastrado.
        if (disponivel >= total) {

            throw new RegraNegocioException(
                    "Estoque inconsistente: não é possível devolver "
                            + "um exemplar acima da quantidade total."
            );
        }

        livro.setQuantidadeDisponivel(disponivel + 1);

        livroRepository.save(livro);
    }
}
