package com.biblioteca.controller;

import com.biblioteca.assembler.EnderecoModelAssembler;
import com.biblioteca.exception.EnderecoNotFoundException;
import com.biblioteca.exception.ErroResponse;
import com.biblioteca.model.Endereco;
import com.biblioteca.repository.EnderecoRepository;
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
@RequestMapping("/enderecos")
@Tag(name = "Endereços", description = "Gerenciamento de dados residenciais e localizações geográficas")
public class EnderecoController {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private EnderecoModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Listar endereços", description = "Recupera uma lista paginada de todos os endereços cadastrados.", operationId = "listarEnderecos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de endereços recuperada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Endereco>>> listar(
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Endereco> pagedResourcesAssembler) {

        Page<Endereco> page = enderecoRepository.findAll(pageable);
        PagedModel<EntityModel<Endereco>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar endereços por cidade", description = "Consulta personalizada que filtra endereços pelo nome da cidade (busca parcial e case-insensitive).", operationId = "buscarEnderecosPorCidade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de busca ou paginação inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Endereco>>> buscarPorCidade(
            @Parameter(description = "Nome da cidade para filtragem parcial", required = true, example = "Taboão da Serra")
            @RequestParam String cidade,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<Endereco> pagedResourcesAssembler) {

        Page<Endereco> page = enderecoRepository.findByCidadeContainingIgnoreCase(cidade, pageable);
        PagedModel<EntityModel<Endereco>> pagedModel = pagedResourcesAssembler.toModel(page, assembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar endereço por ID", description = "Retorna os detalhes de um endereço específico cadastrado na base de dados.", operationId = "buscarEnderecoPorId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endereço encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Endereço não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 404,
                                      "erro": "Não Encontrado",
                                      "mensagem": "Endereço não encontrado com o ID: 99",
                                      "caminho": "/enderecos/99"
                                    }
                                    """)))
    })
    public EntityModel<Endereco> buscarPorId(
            @Parameter(description = "Identificador único do endereço", example = "1")
            @PathVariable Long id) {
        Endereco endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException(id));
        return assembler.toModel(endereco);
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo endereço", description = "Registra um novo endereço com validação de campos e formato do CEP.", operationId = "cadastrarEndereco")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Endereço cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou falha de validação no CEP",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 400,
                                      "erro": "Erro de Validação",
                                      "mensagem": "Um ou mais campos preenchidos são inválidos.",
                                      "caminho": "/enderecos",
                                      "campos": {
                                        "cep": "O CEP deve estar no formato 00000-000 ou 00000000"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<EntityModel<Endereco>> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do novo endereço a ser registrado",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Endereco.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "logradouro": "Rua das Flores",
                                      "numero": "123",
                                      "complemento": "Apto 101",
                                      "bairro": "Centro",
                                      "cidade": "Taboão da Serra",
                                      "estado": "SP",
                                      "cep": "06753-000"
                                    }
                                    """)))
            @Valid @RequestBody Endereco endereco) {
        Endereco novoEndereco = enderecoRepository.save(endereco);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(novoEndereco));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar endereço existente", description = "Atualiza integralmente os dados de um endereço identificado pelo ID.", operationId = "atualizarEndereco")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Endereço não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    })
    public EntityModel<Endereco> atualizar(
            @Parameter(description = "Identificador único do endereço", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados do endereço para substituição",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Endereco.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "logradouro": "Avenida Paulista",
                                      "numero": "1000",
                                      "complemento": "Conjunto 50",
                                      "bairro": "Bela Vista",
                                      "cidade": "Taboão da Serra",
                                      "estado": "SP",
                                      "cep": "06753-100"
                                    }
                                    """)))
            @Valid @RequestBody Endereco endereco) {
        Endereco existente = enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException(id));

        existente.setLogradouro(endereco.getLogradouro());
        existente.setNumero(endereco.getNumero());
        existente.setComplemento(endereco.getComplemento());
        existente.setBairro(endereco.getBairro());
        existente.setCidade(endereco.getCidade());
        existente.setEstado(endereco.getEstado());
        existente.setCep(endereco.getCep());

        Endereco enderecoAtualizado = enderecoRepository.save(existente);
        return assembler.toModel(enderecoAtualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir endereço", description = "Remove um registro de endereço da base de dados se não houver usuários vinculados.", operationId = "excluirEndereco")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Endereço excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Endereço não localizado pelo ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Não foi possível excluir pois o endereço está associado a um usuário cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-13T18:45:00.000+00:00",
                                      "status": 409,
                                      "erro": "Conflito de Regra de Negócio",
                                      "mensagem": "Não foi possível concluir a operação pois o registro possui dependências vinculadas ou viola restrições de unicidade.",
                                      "caminho": "/enderecos/1"
                                    }
                                    """)))
    })
    public void excluir(
            @Parameter(description = "Identificador único do endereço a ser excluído", example = "1")
            @PathVariable Long id) {
        Endereco existente = enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException(id));
        enderecoRepository.delete(existente);
    }
}