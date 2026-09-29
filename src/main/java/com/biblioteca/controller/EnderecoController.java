package com.biblioteca.controller;

import com.biblioteca.assembler.EnderecoModelAssembler;
import com.biblioteca.exception.EnderecoNotFoundException;
import com.biblioteca.model.Endereco;
import com.biblioteca.repository.EnderecoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enderecos")
@Tag(
        name = "Endereços",
        description = "Endpoints para gerenciamento de endereços"
)
public class EnderecoController {

    private final EnderecoRepository enderecoRepository;
    private final EnderecoModelAssembler assembler;

    public EnderecoController(
            EnderecoRepository enderecoRepository,
            EnderecoModelAssembler assembler) {

        this.enderecoRepository = enderecoRepository;
        this.assembler = assembler;
    }

    // GET /enderecos
    @Operation(summary = "Listar endereços")
    @GetMapping
    public Page<EntityModel<Endereco>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return enderecoRepository
                .findAll(pageable)
                .map(assembler::toModel);
    }

    // GET /enderecos/1
    @Operation(summary = "Buscar endereço por ID")
    @GetMapping("/{id}")
    public EntityModel<Endereco> buscarPorId(@PathVariable Long id) {

        Endereco endereco = enderecoRepository.findById(id)
                .orElseThrow(() ->
                        new EnderecoNotFoundException(id)
                );

        return assembler.toModel(endereco);
    }

    // POST /enderecos
    @Operation(summary = "Cadastrar endereço")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<Endereco> cadastrar(
            @Valid @RequestBody Endereco endereco) {

        Endereco novoEndereco = enderecoRepository.save(endereco);

        return assembler.toModel(novoEndereco);
    }

    // PUT /enderecos/1
    @Operation(summary = "Atualizar endereço")
    @PutMapping("/{id}")
    public EntityModel<Endereco> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Endereco endereco) {

        Endereco existente = enderecoRepository.findById(id)
                .orElseThrow(() ->
                        new EnderecoNotFoundException(id)
                );

        existente.setRua(endereco.getRua());
        existente.setNumero(endereco.getNumero());
        existente.setBairro(endereco.getBairro());
        existente.setCidade(endereco.getCidade());
        existente.setEstado(endereco.getEstado());
        existente.setCep(endereco.getCep());

        Endereco enderecoAtualizado =
                enderecoRepository.save(existente);

        return assembler.toModel(enderecoAtualizado);
    }

    // DELETE /enderecos/1
    @Operation(summary = "Excluir endereço")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {

        if (!enderecoRepository.existsById(id)) {
            throw new EnderecoNotFoundException(id);
        }

        enderecoRepository.deleteById(id);
    }
}