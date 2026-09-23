package com.biblioteca.controller;
// Pacote onde está o controller.

import com.biblioteca.model.Livro;
import com.biblioteca.repository.LivroRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/livros")

public class LivroController {

    private final LivroRepository repository;

    public LivroController(LivroRepository repository) {
        this.repository = repository;
    }


    @GetMapping
    // Lista todos os livros -> 200 OK.
    public List<Livro> listar() {
        return repository.findAll();
    }


    @GetMapping("/{id}")
    // Busca pelo ID -> 200 ou 404.
    public Livro buscar(@PathVariable Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Livro não encontrado"
                ));
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // Cadastra um livro -> 201 Created.
    public Livro cadastrar(@Valid @RequestBody Livro livro) {
        return repository.save(livro);
    }


    @PutMapping("/{id}")
    // Edita um livro -> 200 ou 404.
    public Livro editar(
            @PathVariable Long id,
            @Valid @RequestBody Livro livroNovo) {

        Livro livro = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Livro não encontrado"
                ));

        livro.setTitulo(livroNovo.getTitulo());
        livro.setIsbn(livroNovo.getIsbn());
        livro.setAnoPublicacao(livroNovo.getAnoPublicacao());

        return repository.save(livro);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
// Exclui um livro -> 204 ou 404.

    public void excluir(@PathVariable Long id) {

        Livro livro = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Livro não encontrado"
                ));

        repository.delete(livro);
    }
}