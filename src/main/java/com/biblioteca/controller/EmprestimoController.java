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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.hateoas.EntityModel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/emprestimos")
@Tag(
        name = "Empréstimos",
        description = "Endpoints para gerenciamento de empréstimos"
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
    @Operation(
            summary = "Listar empréstimos",
            description = "Retorna os empréstimos cadastrados de forma paginada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Empréstimos listados com sucesso"
    )
    @GetMapping
    public Page<EntityModel<Emprestimo>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return emprestimoRepository
                .findAll(pageable)
                .map(assembler::toModel);
    }


    // CONSULTA PERSONALIZADA - BUSCAR POR STATUS
    @Operation(
            summary = "Buscar empréstimos por status",
            description = "Busca empréstimos pelo status informado: ATIVO, DEVOLVIDO ou ATRASADO."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Busca realizada com sucesso"
    )
    @GetMapping("/buscar")
    public Page<EntityModel<Emprestimo>> buscarPorStatus(
            @Parameter(
                    description = "Status do empréstimo: ATIVO, DEVOLVIDO ou ATRASADO"
            )
            @RequestParam StatusEmprestimo status,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return emprestimoRepository
                .findByStatus(status, pageable)
                .map(assembler::toModel);
    }


    // BUSCAR EMPRÉSTIMO POR ID
    @Operation(
            summary = "Buscar empréstimo por ID",
            description = "Retorna um empréstimo específico a partir do seu ID."
    )
    @GetMapping("/{id}")
    public EntityModel<Emprestimo> buscarPorId(
            @PathVariable Long id) {

        Emprestimo emprestimo = emprestimoRepository
                .findById(id)
                .orElseThrow(() ->
                        new EmprestimoNotFoundException(id)
                );

        return assembler.toModel(emprestimo);
    }


    // CADASTRAR EMPRÉSTIMO
    @Operation(
            summary = "Cadastrar empréstimo",
            description = "Cadastra um novo empréstimo relacionando um livro e um usuário."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    @Operation(
            summary = "Atualizar empréstimo",
            description = "Atualiza os dados de um empréstimo existente."
    )
    @PutMapping("/{id}")
    public EntityModel<Emprestimo> atualizar(
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
    @Operation(
            summary = "Excluir empréstimo",
            description = "Exclui um empréstimo cadastrado a partir do seu ID."
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {

        if (!emprestimoRepository.existsById(id)) {
            throw new EmprestimoNotFoundException(id);
        }

        emprestimoRepository.deleteById(id);
    }


    // REGISTRAR DEVOLUÇÃO
    @Operation(
            summary = "Registrar devolução do empréstimo",
            description = "Altera o status do empréstimo para DEVOLVIDO e registra a data atual como data de devolução."
    )
    @PatchMapping("/{id}/devolver")
    public EntityModel<Emprestimo> devolver(
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