package com.biblioteca.controller;

import com.biblioteca.exception.EmprestimoNotFoundException;
import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.EmprestimoRepository;
import com.biblioteca.repository.LivroRepository;
import com.biblioteca.repository.UsuarioRepository;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public EmprestimoController(
            EmprestimoRepository emprestimoRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository) {

        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // GET /emprestimos
    // Lista os empréstimos com paginação.
    @GetMapping
    public Page<Emprestimo> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return emprestimoRepository.findAll(pageable);
    }

    // GET /emprestimos/1
    // Busca um empréstimo pelo ID.
    @GetMapping("/{id}")
    public Emprestimo buscarPorId(@PathVariable Long id) {

        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNotFoundException(id));
    }

    // POST /emprestimos
    // Cadastra um novo empréstimo.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Emprestimo cadastrar(
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

        return emprestimoRepository.save(emprestimo);
    }

    // PUT /emprestimos/1
    // Atualiza um empréstimo existente.
    @PutMapping("/{id}")
    public Emprestimo atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Emprestimo emprestimo) {

        Emprestimo existente = emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNotFoundException(id));

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

        return emprestimoRepository.save(existente);
    }

    // DELETE /emprestimos/1
    // Exclui um empréstimo pelo ID.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {

        if (!emprestimoRepository.existsById(id)) {
            throw new EmprestimoNotFoundException(id);
        }

        emprestimoRepository.deleteById(id);
    }

    // PATCH /emprestimos/1/devolver
    // Marca o empréstimo como devolvido.
    @PatchMapping("/{id}/devolver")
    public Emprestimo devolver(@PathVariable Long id) {

        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> new EmprestimoNotFoundException(id));

        emprestimo.setDevolvido(true);

        return emprestimoRepository.save(emprestimo);
    }
}