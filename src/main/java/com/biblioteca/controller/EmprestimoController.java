
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
import java.util.List;

@RestController
@RequestMapping("/emprestimos")
@Tag(
        name = "Empréstimos",
        description = "Controle operacional de empréstimos, prazos, devoluções e cancelamentos."
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "401",
                description = "Chave de API ausente, inválida ou inativa",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "A chave de API não possui permissão para esta operação",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
        ),
        @ApiResponse(
                responseCode = "429",
                description = "Limite de requisições excedido",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
        ),
        @ApiResponse(
                responseCode = "405",
                description = "Método HTTP não permitido",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Erro interno no servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
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
            @ApiResponse(responseCode = "200", description = "Empréstimos listados com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
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
            @ApiResponse(responseCode = "200", description = "Empréstimos filtrados por status com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status ausente/inválido ou paginação inválida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
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
            @ApiResponse(responseCode = "200", description = "Empréstimo encontrado com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            )
    })
    public EntityModel<Emprestimo> buscarPorId(
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo emprestimo =
                emprestimoRepository.findById(id)
                        .orElseThrow(() -> new EmprestimoNotFoundException(id));

        return assembler.toModel(emprestimo);
    }

    // =====================================================
    // CADASTRAR EMPRÉSTIMO
    // =====================================================

    @PostMapping
    @Transactional
    @Operation(
            summary = "Cadastrar novo empréstimo",
            description = "Registra um empréstimo quando existem exemplares disponíveis e o usuário não possui outro empréstimo ativo ou atrasado do mesmo livro.",
            operationId = "cadastrarEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empréstimo cadastrado com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário ou livro não localizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de regra de negócio, estoque indisponível ou requisição idempotente ainda em processamento",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Tipo de conteúdo não suportado; utilize application/json",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Chave de idempotência reutilizada com método, rota ou corpo diferente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            )
    })
    public ResponseEntity<EntityModel<Emprestimo>> cadastrar(
            @Parameter(
                    description = "Chave única opcional da requisição.",
                    example = "cadastro-0001"
            )
            @RequestHeader(value = "X-Idempotency-Key", required = false)
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

        if (emprestimo.getDataDevolucao() != null) {
            throw new RegraNegocioException(
                    "Um novo empréstimo não pode possuir data de devolução. "
                            + "A devolução deve ser registrada pela rota específica."
            );
        }

        validarDatas(
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataDevolucaoPrevista()
        );

        Usuario usuario = usuarioRepository.findById(
                emprestimo.getUsuario().getId()
        ).orElseThrow(() ->
                new UsuarioNotFoundException(emprestimo.getUsuario().getId())
        );

        Livro livro = livroRepository.findByIdComBloqueio(
                emprestimo.getLivro().getId()
        ).orElseThrow(() ->
                new LivroNotFoundException(emprestimo.getLivro().getId())
        );

        boolean possuiEmprestimoPendente =
                emprestimoRepository.existsByUsuarioIdAndLivroIdAndStatusIn(
                        usuario.getId(),
                        livro.getId(),
                        List.of(
                                StatusEmprestimo.ATIVO,
                                StatusEmprestimo.ATRASADO
                        )
                );

        if (possuiEmprestimoPendente) {
            throw new RegraNegocioException(
                    "Este usuário já possui um empréstimo ativo ou atrasado deste livro."
            );
        }

        if (livro.getQuantidadeDisponivel() <= 0) {
            throw new RegraNegocioException(
                    "Não há exemplares disponíveis deste livro para empréstimo."
            );
        }

        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);

        if (emprestimo.getStatus() == null) {
            emprestimo.setStatus(StatusEmprestimo.ATIVO);
        } else if (emprestimo.getStatus() != StatusEmprestimo.ATIVO) {
            throw new RegraNegocioException(
                    "Um novo empréstimo deve ser cadastrado com status ATIVO."
            );
        }

        livro.setQuantidadeDisponivel(
                livro.getQuantidadeDisponivel() - 1
        );

        livroRepository.save(livro);

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
    @Transactional
    @Operation(
            summary = "Atualizar empréstimo existente",
            description = "Atualiza os dados gerais de um empréstimo sem permitir alteração direta do status, troca do livro ou registro direto de devolução.",
            operationId = "atualizarEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empréstimo atualizado com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID ou dados inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo ou usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de regra de negócio ou integridade",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Tipo de conteúdo não suportado; utilize application/json",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            )
    })
    public EntityModel<Emprestimo> atualizar(
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id,

            @Valid @RequestBody EmprestimoRequestDTO dto) {

        Emprestimo dadosRecebidos = dto.toEntity();

        Emprestimo existente =
                emprestimoRepository.findByIdComBloqueio(id)
                        .orElseThrow(() -> new EmprestimoNotFoundException(id));

        if (dadosRecebidos.getDataDevolucao() != null) {
            throw new RegraNegocioException(
                    "A data de devolução não pode ser alterada diretamente. "
                            + "Utilize a rota específica de devolução."
            );
        }

        if (dadosRecebidos.getStatus() != null
                && dadosRecebidos.getStatus() != existente.getStatus()) {
            throw new RegraNegocioException(
                    "A situação do empréstimo deve ser alterada pelas rotas "
                            + "específicas de devolução ou cancelamento."
            );
        }

        if (dadosRecebidos.getLivro() != null
                && dadosRecebidos.getLivro().getId() != null
                && !dadosRecebidos.getLivro().getId()
                .equals(existente.getLivro().getId())) {
            throw new RegraNegocioException(
                    "Não é permitido trocar o livro de um empréstimo já cadastrado."
            );
        }

        validarDatas(
                dadosRecebidos.getDataEmprestimo(),
                dadosRecebidos.getDataDevolucaoPrevista()
        );

        if (dadosRecebidos.getUsuario() != null
                && dadosRecebidos.getUsuario().getId() != null) {

            Usuario usuario = usuarioRepository.findById(
                    dadosRecebidos.getUsuario().getId()
            ).orElseThrow(() ->
                    new UsuarioNotFoundException(dadosRecebidos.getUsuario().getId())
            );

            existente.setUsuario(usuario);
        }

        existente.setDataEmprestimo(dadosRecebidos.getDataEmprestimo());
        existente.setDataDevolucaoPrevista(
                dadosRecebidos.getDataDevolucaoPrevista()
        );

        Emprestimo emprestimoAtualizado =
                emprestimoRepository.save(existente);

        return assembler.toModel(emprestimoAtualizado);
    }

    // =====================================================
    // DEVOLVER EMPRÉSTIMO
    // =====================================================

    @PatchMapping("/{id}/devolver")
    @Transactional
    @Operation(
            summary = "Devolver livro do empréstimo",
            description = "Registra a devolução e restaura uma unidade ao estoque. Não permite devolução duplicada.",
            operationId = "devolverEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolução registrada com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo ou livro não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Empréstimo não pode ser devolvido ou estoque inconsistente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            )
    })
    public EntityModel<Emprestimo> devolver(
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo existente =
                emprestimoRepository.findByIdComBloqueio(id)
                        .orElseThrow(() -> new EmprestimoNotFoundException(id));

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

        restaurarEstoque(existente.getLivro().getId());

        existente.setDataDevolucao(LocalDate.now());
        existente.setStatus(StatusEmprestimo.DEVOLVIDO);

        Emprestimo atualizado = emprestimoRepository.save(existente);

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
            @ApiResponse(responseCode = "200", description = "Empréstimo cancelado com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo ou livro não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Empréstimo não pode ser cancelado ou estoque inconsistente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            )
    })
    public EntityModel<Emprestimo> cancelar(
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo existente =
                emprestimoRepository.findByIdComBloqueio(id)
                        .orElseThrow(() -> new EmprestimoNotFoundException(id));

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

        restaurarEstoque(existente.getLivro().getId());

        existente.setStatus(StatusEmprestimo.CANCELADO);

        Emprestimo cancelado = emprestimoRepository.save(existente);

        return assembler.toModel(cancelado);
    }

    // =====================================================
    // EXCLUIR EMPRÉSTIMO
    // =====================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(
            summary = "Excluir empréstimo",
            description = "Remove um registro. Se estiver ativo ou atrasado, restaura uma unidade ao estoque.",
            operationId = "excluirEmprestimo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Empréstimo excluído com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empréstimo ou livro não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de integridade ou estoque inconsistente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))
            )
    })
    public void excluir(
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Emprestimo existente =
                emprestimoRepository.findByIdComBloqueio(id)
                        .orElseThrow(() -> new EmprestimoNotFoundException(id));

        if (existente.getStatus() == StatusEmprestimo.ATIVO
                || existente.getStatus() == StatusEmprestimo.ATRASADO) {
            restaurarEstoque(existente.getLivro().getId());
        }

        emprestimoRepository.delete(existente);
    }

    // =====================================================
    // VALIDAR DATAS
    // =====================================================

    private void validarDatas(
            LocalDate dataEmprestimo,
            LocalDate dataDevolucaoPrevista) {

        if (dataEmprestimo == null || dataDevolucaoPrevista == null) {
            throw new RegraNegocioException(
                    "A data do empréstimo e a data prevista de devolução são obrigatórias."
            );
        }

        if (dataDevolucaoPrevista.isBefore(dataEmprestimo)) {
            throw new RegraNegocioException(
                    "A data de devolução prevista não pode ser anterior à data de empréstimo."
            );
        }
    }

    // =====================================================
    // RESTAURAR ESTOQUE
    // =====================================================

    private void restaurarEstoque(Long livroId) {

        Livro livro = livroRepository.findByIdComBloqueio(livroId)
                .orElseThrow(() -> new LivroNotFoundException(livroId));

        int total = livro.getQuantidadeTotal();
        int disponivel = livro.getQuantidadeDisponivel();

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
