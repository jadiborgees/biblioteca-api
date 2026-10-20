package com.biblioteca.controller;

import com.biblioteca.assembler.UsuarioModelAssembler;
import com.biblioteca.exception.*;
import com.biblioteca.model.Endereco;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.EnderecoRepository;
import com.biblioteca.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
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

import java.util.Set;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Contas de leitores vinculadas a endereços cadastrados")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private UsuarioModelAssembler assembler;

    @Autowired
    private Validator validator;

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
    @Operation(summary = "Buscar usuários por nome", description = "Consulta personalizada que filtra usuários pelo nome (busca parcial e case-insensitive).", operationId = "buscarUsuariosPorNome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca ou paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> buscarPorNome(
            @Parameter(description = "Nome do usuário para filtragem parcial", required = true, example = "João")
            @RequestParam String nome,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<Usuario> pagedResourcesAssembler) {

        Page<Usuario> page = usuarioRepository.findByNomeContainingIgnoreCase(nome, pageable);
        PagedModel<EntityModel<Usuario>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os detalhes de um usuário específico.", operationId = "buscarUsuarioPorId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 404,
                                      "erro": "Não Encontrado",
                                      "mensagem": "Usuário não encontrado com o ID: 99",
                                      "caminho": "/usuarios/99"
                                    }
                                    """)))
    })
    public EntityModel<Usuario> buscarPorId(
            @Parameter(description = "Identificador único do usuário", example = "1")
            @PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        return assembler.toModel(usuario);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo usuário", description = "Registra um novo usuário. Aceita endereço embutido ou referência por ID.", operationId = "cadastrarUsuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou idade fora do intervalo de 18 a 120 anos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 400,
                                      "erro": "Erro de Validação",
                                      "mensagem": "Um ou mais campos preenchidos são inválidos.",
                                      "caminho": "/usuarios",
                                      "campos": {
                                        "idadeValida": "O usuário deve ter entre 18 e 120 anos"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Endereço informado por ID não localizado na base de dados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: e-mail já cadastrado na base de dados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 409,
                                      "erro": "Conflito de Regra de Negócio",
                                      "mensagem": "Não foi possível concluir a operação pois o registro possui dependências vinculadas ou viola restrições de unicidade.",
                                      "caminho": "/usuarios"
                                    }
                                    """)))
    })
    public ResponseEntity<EntityModel<Usuario>> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para cadastro do usuário",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Usuario.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "nome": "João Silva",
                                      "email": "joao@email.com",
                                      "telefone": "11999999999",
                                      "dataNascimento": "1995-05-12",
                                      "endereco": {
                                        "logradouro": "Rua das Flores",
                                        "numero": "123",
                                        "bairro": "Centro",
                                        "cidade": "São Paulo",
                                        "estado": "SP",
                                        "cep": "01001-000"
                                      }
                                    }
                                    """)))
            @Valid @RequestBody Usuario usuario) {

        Endereco end = usuario.getEndereco();
        if (end != null) {
            if (end.getId() != null) {
                Endereco endExistente = enderecoRepository.findById(end.getId())
                        .orElseThrow(() -> new EnderecoNotFoundException(end.getId()));
                usuario.setEndereco(endExistente);
            } else {
                Set<ConstraintViolation<Endereco>> erros = validator.validate(end);
                if (!erros.isEmpty()) {
                    throw new ConstraintViolationException(erros);
                }
            }
        }

        Usuario novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(novoUsuario));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário existente", description = "Atualiza integralmente os dados de um usuário.", operationId = "atualizarUsuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ou Endereço não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: e-mail já pertence a outro usuário",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntityModel<Usuario> atualizar(
            @Parameter(description = "Identificador único do usuário", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados para atualização do usuário",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Usuario.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "nome": "João Silva Santos",
                                      "email": "joaosilva@email.com",
                                      "telefone": "11988887777",
                                      "dataNascimento": "1995-05-12",
                                      "endereco": {
                                        "id": 1
                                      }
                                    }
                                    """)))
            @Valid @RequestBody Usuario usuario) {

        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        existente.setNome(usuario.getNome());
        existente.setEmail(usuario.getEmail());
        existente.setTelefone(usuario.getTelefone());
        existente.setDataNascimento(usuario.getDataNascimento());

        Endereco end = usuario.getEndereco();
        if (end != null) {
            if (end.getId() != null) {
                Endereco endExistente = enderecoRepository.findById(end.getId())
                        .orElseThrow(() -> new EnderecoNotFoundException(end.getId()));
                existente.setEndereco(endExistente);
            } else {
                Set<ConstraintViolation<Endereco>> erros = validator.validate(end);
                if (!erros.isEmpty()) {
                    throw new ConstraintViolationException(erros);
                }
                existente.setEndereco(end);
            }
        }

        Usuario usuarioAtualizado = usuarioRepository.save(existente);
        return assembler.toModel(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir usuário", description = "Remove um usuário da base de dados se não houver empréstimos vinculados.", operationId = "excluirUsuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: usuário possui empréstimos associados",
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