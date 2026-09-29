package com.biblioteca.controller;

import com.biblioteca.assembler.EmprestimoModelAssembler;
import com.biblioteca.exception.EmprestimoNotFoundException;
import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.EmprestimoRepository;
import com.biblioteca.repository.LivroRepository;
import com.biblioteca.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
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

    // GET /emprestimos
    @Operation(summary = "Listar empréstimos")
    @GetMapping
    public Page<EntityModel<Emprestimo>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return emprestimoRepository
                .findAll(pageable)
                .map(assembler::toModel);
    }

    // GET /emprestimos/1
    @Operation(summary = "Buscar empréstimo por ID")
    @GetMapping("/{id}")
    public EntityModel<Emprestimo> buscarPorId(@PathVariable Long id) {

        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() ->
                        new EmprestimoNotFoundException(id)
                );

        return assembler.toModel(emprestimo);
    }

    // POST /emprestimos
    @Operation(summary = "Cadastrar empréstimo")
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

    // PUT /emprestimos/1
    @Operation(summary = "Atualizar empréstimo")
    @PutMapping("/{id}")
    public EntityModel<Emprestimo> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Emprestimo emprestimo) {

        Emprestimo existente = emprestimoRepository.findById(id)
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
        existente.setDevolvido(emprestimo.isDevolvido());
        existente.setLivro(livro);
        existente.setUsuario(usuario);

        Emprestimo emprestimoAtualizado =
                emprestimoRepository.save(existente);

        return assembler.toModel(emprestimoAtualizado);
    }

    // DELETE /emprestimos/1
    @Operation(summary = "Excluir empréstimo")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {

        if (!emprestimoRepository.existsById(id)) {
            throw new EmprestimoNotFoundException(id);
        }

        emprestimoRepository.deleteById(id);
    }

    // PATCH /emprestimos/1/devolver
    @Operation(summary = "Registrar devolução do empréstimo")
    @PatchMapping("/{id}/devolver")
    public EntityModel<Emprestimo> devolver(@PathVariable Long id) {

        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() ->
                        new EmprestimoNotFoundException(id)
                );

        emprestimo.setDevolvido(true);

        Emprestimo emprestimoDevolvido =
                emprestimoRepository.save(emprestimo);

        return assembler.toModel(emprestimoDevolvido);
    }
}