package com.biblioteca.controller;

import com.biblioteca.assembler.UsuarioModelAssembler;
import com.biblioteca.exception.ErroResponse;
import com.biblioteca.exception.UsuarioNotFoundException;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Gestão de usuários da biblioteca e seus endereços")
@ApiResponses(value = {
        @ApiResponse(responseCode = "405", description = "Método HTTP não permitido para esta rota",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
})
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Recupera uma lista paginada de todos os usuários cadastrados.", operationId = "listarUsuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários recuperada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Usuario> pagedResourcesAssembler) {

        Page<Usuario> page = usuarioRepository.findAll(pageable);
        PagedModel<EntityModel<Usuario>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar usuários por nome", description = "Consulta personalizada que filtra usuários pelo nome contendo o termo pesquisado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca ou paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> buscarPorNome(
            @Parameter(description = "Nome do usuário para busca", required = true, example = "Maria")
            @RequestParam String nome,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Usuario> pagedResourcesAssembler) {

        Page<Usuario> page = usuarioRepository.findByNomeContainingIgnoreCase(nome, pageable);
        PagedModel<EntityModel<Usuario>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os detalhes de um usuário específico juntamente com seu endereço.", operationId = "buscarUsuarioPorId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntityModel<Usuario> buscarPorId(
            @Parameter(description = "Identificador único do usuário", example = "1")
            @PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        return assembler.toModel(usuario);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo usuário", description = "Cadastra um novo usuário juntamente com seu endereço residencial em cascata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: e-mail já cadastrado no sistema",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<EntityModel<Usuario>> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do usuário e endereço para cadastro",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Usuario.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "nome": "Maria Oliveira",
                                      "email": "maria.oliveira@email.com",
                                      "telefone": "11988887777",
                                      "dataNascimento": "1995-08-20",
                                      "endereco": {
                                        "logradouro": "Avenida Paulista",
                                        "numero": "1000",
                                        "complemento": "Apto 42",
                                        "bairro": "Bela Vista",
                                        "cidade": "São Paulo",
                                        "estado": "SP",
                                        "cep": "01310-100"
                                      }
                                    }
                                    """)))
            @Valid @RequestBody Usuario usuario) {

        Usuario novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(novoUsuario));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário existente", description = "Atualiza os dados cadastrais e endereço de um usuário existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: e-mail já pertence a outro usuário",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntityModel<Usuario> atualizar(
            @Parameter(description = "Identificador único do usuário", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Usuario usuario) {

        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        existente.setNome(usuario.getNome());
        existente.setEmail(usuario.getEmail());
        existente.setTelefone(usuario.getTelefone());
        existente.setDataNascimento(usuario.getDataNascimento());

        if (usuario.getEndereco() != null) {
            existente.setEndereco(usuario.getEndereco());
        }

        Usuario usuarioAtualizado = usuarioRepository.save(existente);
        return assembler.toModel(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir usuário", description = "Remove um usuário e seu endereço associado da base de dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade: não é possível excluir usuário com empréstimos vinculados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public void excluir(
            @Parameter(description = "Identificador único do usuário a ser excluído", example = "1")
            @PathVariable Long id) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        usuarioRepository.delete(existente);
    }
}