package com.biblioteca.controller;

import com.biblioteca.model.Endereco;
import com.biblioteca.repository.EnderecoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private final EnderecoRepository enderecoRepository;


    public EnderecoController(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }


    @GetMapping
    // GET /enderecos -> lista os endereços com paginação
    public Page<Endereco> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return enderecoRepository.findAll(pageable);
    }


    @GetMapping("/{id}")
    // GET /enderecos/1 -> busca um endereço pelo ID
    public Endereco buscarPorId(@PathVariable Long id) {

        return enderecoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Endereço não encontrado"
                        )
                );
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // POST /enderecos -> cadastra um novo endereço
    public Endereco cadastrar(@RequestBody Endereco endereco) {

        return enderecoRepository.save(endereco);
    }


    @PutMapping("/{id}")
    // PUT /enderecos/1 -> atualiza um endereço
    public Endereco atualizar(
            @PathVariable Long id,
            @RequestBody Endereco endereco) {

        Endereco existente = enderecoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Endereço não encontrado"
                        )
                );

        existente.setRua(endereco.getRua());
        existente.setNumero(endereco.getNumero());
        existente.setBairro(endereco.getBairro());
        existente.setCidade(endereco.getCidade());
        existente.setEstado(endereco.getEstado());
        existente.setCep(endereco.getCep());

        return enderecoRepository.save(existente);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // DELETE /enderecos/1 -> exclui um endereço
    public void excluir(@PathVariable Long id) {

        if (!enderecoRepository.existsById(id)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Endereço não encontrado"
            );
        }

        enderecoRepository.deleteById(id);
    }
}