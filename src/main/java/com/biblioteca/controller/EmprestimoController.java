package com.biblioteca.controller;

import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.EmprestimoRepository;
import com.biblioteca.repository.LivroRepository;
import com.biblioteca.repository.UsuarioRepository;

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


    @GetMapping
    // GET /emprestimos -> lista os empréstimos com paginação
    public Page<Emprestimo> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return emprestimoRepository.findAll(pageable);
    }


    @GetMapping("/{id}")
    // GET /emprestimos/1 -> busca um empréstimo pelo ID
    public Emprestimo buscarPorId(@PathVariable Long id) {

        return emprestimoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Empréstimo não encontrado"
                        )
                );
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // POST /emprestimos -> cadastra um novo empréstimo
    public Emprestimo cadastrar(@RequestBody Emprestimo emprestimo) {

        if (emprestimo.getLivro() == null ||
                emprestimo.getLivro().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o livro do empréstimo"
            );
        }


        if (emprestimo.getUsuario() == null ||
                emprestimo.getUsuario().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o usuário do empréstimo"
            );
        }


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


    @PutMapping("/{id}")
    // PUT /emprestimos/1 -> atualiza um empréstimo
    public Emprestimo atualizar(
            @PathVariable Long id,
            @RequestBody Emprestimo emprestimo) {

        Emprestimo existente = emprestimoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Empréstimo não encontrado"
                        )
                );


        if (emprestimo.getLivro() == null ||
                emprestimo.getLivro().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o livro do empréstimo"
            );
        }


        if (emprestimo.getUsuario() == null ||
                emprestimo.getUsuario().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o usuário do empréstimo"
            );
        }


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


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // DELETE /emprestimos/1 -> exclui um empréstimo
    public void excluir(@PathVariable Long id) {

        if (!emprestimoRepository.existsById(id)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empréstimo não encontrado"
            );
        }

        emprestimoRepository.deleteById(id);
    }


    @PatchMapping("/{id}/devolver")
    // PATCH /emprestimos/1/devolver -> registra a devolução
    public Emprestimo devolver(@PathVariable Long id) {

        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Empréstimo não encontrado"
                        )
                );

        emprestimo.setDevolvido(true);

        return emprestimoRepository.save(emprestimo);
    }
}