package com.biblioteca.controller;

import com.biblioteca.assembler.UsuarioModelAssembler;
import com.biblioteca.exception.UsuarioNotFoundException;
import com.biblioteca.model.Endereco;
import com.biblioteca.model.IdempotencyKey;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.EnderecoRepository;
import com.biblioteca.repository.IdempotencyKeyRepository;
import com.biblioteca.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/usuarios")
@Tag(
        name = "Usuários",
        description = "Endpoints para cadastro, consulta, atualização e exclusão dos usuários da biblioteca"
)
public class UsuarioController {

    private final UsuarioRepository repository;
    private final EnderecoRepository enderecoRepository;
    private final UsuarioModelAssembler assembler;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public UsuarioController(
            UsuarioRepository repository,
            EnderecoRepository enderecoRepository,
            UsuarioModelAssembler assembler,
            IdempotencyKeyRepository idempotencyKeyRepository) {

        this.repository = repository;
        this.enderecoRepository = enderecoRepository;
        this.assembler = assembler;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }


    // LISTAR USUÁRIOS
    @GetMapping
    @Operation(
            summary = "Listar usuários",
            description = "Retorna todos os usuários cadastrados de forma paginada."
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


    // BUSCAR USUÁRIOS POR NOME
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar usuários por nome",
            description = "Busca usuários que contenham o texto informado no nome. A busca não diferencia letras maiúsculas e minúsculas e o resultado é paginado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetro de busca inválido"
            )
    })
    public PagedModel<EntityModel<Usuario>> buscarPorNome(

            @Parameter(
                    description = "Nome completo ou parte do nome do usuário",
                    example = "Maria"
            )
            @RequestParam String nome,

            @ParameterObject Pageable pageable,

            PagedResourcesAssembler<Usuario> pagedAssembler) {

        Page<Usuario> usuarios =
                repository.findByNomeContainingIgnoreCase(
                        nome,
                        pageable
                );

        return pagedAssembler.toModel(usuarios, assembler);
    }


    // BUSCAR USUÁRIO POR ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os dados de um usuário específico a partir do seu identificador."
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

            @Parameter(
                    description = "ID do usuário",
                    example = "1"
            )
            @PathVariable Long id) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNotFoundException(id)
                );

        return assembler.toModel(usuario);
    }


    // CADASTRAR USUÁRIO COM IDEMPOTÊNCIA
    @PostMapping
    @Operation(
            summary = "Cadastrar usuário",
            description = "Cadastra um novo usuário utilizando o header X-Idempotency-Key para impedir que a mesma operação seja processada duas vezes. Caso um endereço seja informado, ele deve estar previamente cadastrado."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do usuário que será cadastrado",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "nome": "Maria Silva",
                                      "email": "maria@email.com",
                                      "telefone": "11999999999",
                                      "endereco": {
                                        "id": 1
                                      }
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Requisição já processada anteriormente. O usuário existente foi retornado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do usuário inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço informado não encontrado"
            )
    })
    public ResponseEntity<EntityModel<Usuario>> cadastrar(

            @Parameter(
                    description = "Chave única utilizada para impedir o processamento duplicado da requisição",
                    example = "usuario-001",
                    required = true
            )
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,

            @Valid @RequestBody Usuario usuario) {

        // Prefixo evita conflito com chaves de outras entidades.
        String chaveInterna = "usuario:" + idempotencyKey;

        // Verifica se essa operação já foi processada.
        if (idempotencyKeyRepository.existsById(chaveInterna)) {

            IdempotencyKey chaveExistente =
                    idempotencyKeyRepository.findById(chaveInterna)
                            .orElseThrow();

            Long usuarioId =
                    Long.valueOf(chaveExistente.getRecursoId());

            Usuario usuarioExistente = repository.findById(usuarioId)
                    .orElseThrow(() ->
                            new UsuarioNotFoundException(usuarioId)
                    );

            // Já existia: retorna o mesmo usuário com 200 OK.
            return ResponseEntity.ok(
                    assembler.toModel(usuarioExistente)
            );
        }

        // Se um endereço foi informado, busca o endereço existente.
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

        // Primeira requisição: cadastra o usuário.
        Usuario novoUsuario = repository.save(usuario);

        // Registra a chave e o ID do usuário criado.
        IdempotencyKey novaChave =
                new IdempotencyKey(
                        chaveInterna,
                        novoUsuario.getId().toString()
                );

        idempotencyKeyRepository.save(novaChave);

        // Novo recurso criado: 201 Created.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(novoUsuario));
    }


    // ATUALIZAR USUÁRIO
    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza nome, e-mail, telefone e endereço de um usuário existente."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do usuário",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "nome": "Maria Silva",
                                      "email": "maria.silva@email.com",
                                      "telefone": "11988888888",
                                      "endereco": {
                                        "id": 1
                                      }
                                    }
                                    """
                    )
            )
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

            @Parameter(
                    description = "ID do usuário que será atualizado",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid @RequestBody Usuario usuario) {

        Usuario usuarioExistente = repository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNotFoundException(id)
                );

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

        Usuario usuarioAtualizado =
                repository.save(usuarioExistente);

        return assembler.toModel(usuarioAtualizado);
    }


    // EXCLUIR USUÁRIO
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir usuário",
            description = "Exclui permanentemente um usuário cadastrado a partir do seu ID."
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

            @Parameter(
                    description = "ID do usuário que será excluído",
                    example = "1"
            )
            @PathVariable Long id) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNotFoundException(id)
                );

        // Desvincula o endereço antes de excluir o usuário.
        usuario.setEndereco(null);
        repository.save(usuario);

        // Exclui o usuário.
        repository.delete(usuario);
    }
}