package com.biblioteca.controller;

import com.biblioteca.exception.EnderecoNotFoundException;
import com.biblioteca.model.Endereco;
import com.biblioteca.repository.EnderecoRepository;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private final EnderecoRepository enderecoRepository;

    public EnderecoController(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    // GET /enderecos
    // Lista os endereços com paginação.
    @GetMapping
    public Page<Endereco> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return enderecoRepository.findAll(pageable);
    }

    // GET /enderecos/1
    // Busca um endereço pelo ID.
    @GetMapping("/{id}")
    public Endereco buscarPorId(@PathVariable Long id) {

        return enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException(id));
    }

    // POST /enderecos
    // Cadastra um novo endereço.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Endereco cadastrar(
            @Valid @RequestBody Endereco endereco) {

        return enderecoRepository.save(endereco);
    }

    // PUT /enderecos/1
    // Atualiza um endereço existente.
    @PutMapping("/{id}")
    public Endereco atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Endereco endereco) {

        Endereco existente = enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException(id));

        existente.setRua(endereco.getRua());
        existente.setNumero(endereco.getNumero());
        existente.setBairro(endereco.getBairro());
        existente.setCidade(endereco.getCidade());
        existente.setEstado(endereco.getEstado());
        existente.setCep(endereco.getCep());

        return enderecoRepository.save(existente);
    }

    // DELETE /enderecos/1
    // Exclui um endereço pelo ID.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {

        if (!enderecoRepository.existsById(id)) {
            throw new EnderecoNotFoundException(id);
        }

        enderecoRepository.deleteById(id);
    }
}