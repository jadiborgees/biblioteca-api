package com.biblioteca.controller;

import com.biblioteca.assembler.UsuarioModelAssembler;
import com.biblioteca.exception.UsuarioNotFoundException;
import com.biblioteca.model.Endereco;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.EnderecoRepository;
import com.biblioteca.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/usuarios")
@Tag(
        name = "Usuários",
        description = "Endpoints para gerenciamento dos usuários da biblioteca"
)
public class UsuarioController {

    private final UsuarioRepository repository;
    private final EnderecoRepository enderecoRepository;
    private final UsuarioModelAssembler assembler;

    public UsuarioController(
            UsuarioRepository repository,
            EnderecoRepository enderecoRepository,
            UsuarioModelAssembler assembler) {

        this.repository = repository;
        this.enderecoRepository = enderecoRepository;
        this.assembler = assembler;
    }


    @GetMapping
    @Operation(
            summary = "Listar usuários",
            description = "Retorna os usuários cadastrados de forma paginada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Usuários listados com sucesso"
    )
    public PagedModel<EntityModel<Usuario>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Usuario> pagedAssembler) {

        Page<Usuario> usuarios = repository.findAll(pageable);

        return pagedAssembler.toModel(usuarios, assembler);
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna um usuário específico a partir do seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            )
    })
    public EntityModel<Usuario> buscarPorId(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        return assembler.toModel(usuario);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar usuário",
            description = "Cadastra um novo usuário na biblioteca."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do usuário inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não encontrado"
            )
    })
    public EntityModel<Usuario> cadastrar(
            @Valid @RequestBody Usuario usuario) {

        if (usuario.getEndereco() != null) {

            Endereco endereco = enderecoRepository
                    .findById(usuario.getEndereco().getId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Endereço não encontrado"
                            )
                    );

            usuario.setEndereco(endereco);
        }

        Usuario novoUsuario = repository.save(usuario);

        return assembler.toModel(novoUsuario);
    }


    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza os dados de um usuário existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do usuário inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário ou endereço não encontrado"
            )
    })
    public EntityModel<Usuario> atualizar(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id,
            @Valid @RequestBody Usuario usuario) {

        Usuario usuarioExistente = repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        usuarioExistente.setNome(usuario.getNome());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setTelefone(usuario.getTelefone());

        if (usuario.getEndereco() != null) {

            Endereco endereco = enderecoRepository
                    .findById(usuario.getEndereco().getId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Endereço não encontrado"
                            )
                    );

            usuarioExistente.setEndereco(endereco);
        }

        Usuario usuarioAtualizado = repository.save(usuarioExistente);

        return assembler.toModel(usuarioAtualizado);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir usuário",
            description = "Exclui um usuário cadastrado a partir do seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            )
    })
    public void excluir(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        repository.delete(usuario);
    }
}