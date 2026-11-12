
package com.biblioteca.controller;

import com.biblioteca.assembler.EnderecoModelAssembler;
import com.biblioteca.dto.EnderecoRequestDTO;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
@Tag(
        name = "Endereços",
        description = "Gestão de endereços residenciais vinculados aos usuários"
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "401",
                description = "Não autenticado. Chave de API ausente ou inválida.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErroResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Acesso proibido. O nível de acesso não permite executar esta operação.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErroResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "429",
                description = "Limite de requisições excedido. Aguarde antes de tentar novamente.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErroResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "405",
                description = "Método HTTP não permitido para esta rota",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErroResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Erro interno no servidor",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErroResponse.class)
                )
        )
})
public class EnderecoController {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private EnderecoModelAssembler assembler;

    @GetMapping
    @Operation(
            summary = "Listar endereços",
            description = "Recupera uma lista paginada de todos os endereços cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de endereços recuperada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de paginação inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<PagedModel<EntityModel<Endereco>>> listar(
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true)
            PagedResourcesAssembler<Endereco> pagedResourcesAssembler) {

        Page<Endereco> page = enderecoRepository.findAll(pageable);

        PagedModel<EntityModel<Endereco>> pagedModel =
                pagedResourcesAssembler.toModel(page, assembler);

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar endereços por cidade",
            description = "Consulta personalizada que busca endereços cadastrados em uma determinada cidade."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cidade obrigatória ou parâmetros de paginação inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<PagedModel<EntityModel<Endereco>>> buscarPorCidade(
            @Parameter(
                    description = "Nome da cidade para pesquisa",
                    required = true,
                    example = "São Paulo"
            )
            @RequestParam
            @NotBlank(message = "O parâmetro 'cidade' é obrigatório.")
            String cidade,

            @ParameterObject Pageable pageable,

            @Parameter(hidden = true)
            PagedResourcesAssembler<Endereco> pagedResourcesAssembler) {

        Page<Endereco> page =
                enderecoRepository.findByCidadeContainingIgnoreCase(cidade, pageable);

        PagedModel<EntityModel<Endereco>> pagedModel =
                pagedResourcesAssembler.toModel(page, assembler);

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar endereço por ID",
            description = "Retorna os detalhes de um endereço específico."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Endereço localizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido. O identificador deve ser positivo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public EntityModel<Endereco> buscarPorId(
            @Parameter(
                    description = "Identificador único do endereço",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Endereco endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException(id));

        return assembler.toModel(endereco);
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar novo endereço",
            description = "Cadastra um novo endereço no sistema com suporte à idempotência (X-Idempotency-Key)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Endereço cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos, CEP fora do padrão ou JSON malformado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A primeira requisição com a mesma chave de idempotência ainda está em processamento ou ocorreu conflito de integridade.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Tipo de conteúdo não suportado. Utilize application/json.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Chave X-Idempotency-Key reutilizada com método, rota ou corpo diferentes.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<EntityModel<Endereco>> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do endereço para cadastro",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = Endereco.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "logradouro": "Avenida Paulista",
                                      "numero": "1000",
                                      "complemento": "Apto 42",
                                      "bairro": "Bela Vista",
                                      "cidade": "São Paulo",
                                      "estado": "SP",
                                      "cep": "01310-100"
                                    }
                                    """)
                    )
            )
            @Parameter(
                    description = "Chave única do cliente (1 a 255 caracteres: letras, números, '-', '_', ':' ou '.'). Repetir o envio com a mesma chave e o mesmo corpo devolve a resposta original.",
                    example = "cadastro-0001"
            )
            @RequestHeader(
                    value = "X-Idempotency-Key",
                    required = false
            )
            String idempotencyKey,

            @Valid @RequestBody EnderecoRequestDTO dto) {

        Endereco endereco = dto.toEntity();

        Endereco novoEndereco = enderecoRepository.save(endereco);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(novoEndereco));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar endereço existente",
            description = "Atualiza os dados de um endereço cadastrado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Endereço atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID ou dados de atualização inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de integridade de dados durante a atualização do endereço.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Tipo de conteúdo não suportado. Utilize application/json.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public EntityModel<Endereco> atualizar(
            @Parameter(
                    description = "Identificador único do endereço",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id,

            @Valid @RequestBody EnderecoRequestDTO dto) {

        Endereco endereco = dto.toEntity();

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
    @Operation(
            summary = "Excluir endereço",
            description = "Remove um endereço da base de dados. A exclusão pode ser impedida caso existam relacionamentos que violem a integridade referencial."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Endereço excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido. O identificador deve ser positivo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de integridade: não é possível excluir o endereço enquanto houver usuário ou outra referência vinculada que impeça a exclusão.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public void excluir(
            @Parameter(
                    description = "Identificador único do endereço a ser excluído",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo.")
            Long id) {

        Endereco existente = enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException(id));

        enderecoRepository.delete(existente);
    }
}
