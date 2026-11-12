
package com.biblioteca.controller;

import com.biblioteca.assembler.LivroModelAssembler;
import com.biblioteca.dto.LivroRequestDTO;
import com.biblioteca.dto.LivroV2DTO;
import com.biblioteca.dto.PaginaResponseDTO;
import com.biblioteca.dto.ReferenciaDTO;
import com.biblioteca.exception.AutorNotFoundException;
import com.biblioteca.exception.ErroResponse;
import com.biblioteca.exception.LivroNotFoundException;
import com.biblioteca.exception.RequisicaoInvalidaException;
import com.biblioteca.exception.UnsupportedApiVersionException;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LivroRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/livros")
@Tag(
        name = "Livros",
        description = "Gerenciamento do acervo de livros da biblioteca"
)
@ApiResponses(value = {
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
public class LivroController {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private LivroModelAssembler assembler;

    // =====================================================
    // LISTAGEM V1
    // =====================================================

    @GetMapping(headers = "X-API-Version!=2")
    @Operation(
            summary = "Listar livros (v1)",
            description = "Recupera uma lista paginada de livros no formato HATEOAS. "
                    + "É a versão padrão quando X-API-Version não é informado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de livros recuperada com sucesso",
                    headers = @Header(
                            name = "X-API-Version",
                            description = "Versão da API utilizada",
                            schema = @Schema(type = "string", example = "1.0")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Versão de API ou parâmetros de paginação inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> listar(
            @ParameterObject Pageable pageable,

            @Parameter(hidden = true)
            PagedResourcesAssembler<Livro> pagedResourcesAssembler,

            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-API-Version",
                    required = false,
                    description = "Versão da API: 1 ou 1.0 (padrão).",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"1", "1.0", "2"},
                            example = "1"
                    )
            )
            @RequestHeader(
                    value = "X-API-Version",
                    required = false
            )
            String versao) {

        if (versao != null
                && !versao.isBlank()
                && !List.of("1", "1.0").contains(versao.trim())) {

            throw new UnsupportedApiVersionException(
                    "Versão de API '" + versao
                            + "' não suportada. Use X-API-Version com 1 ou 2."
            );
        }

        Page<Livro> page = livroRepository.findAll(pageable);

        PagedModel<EntityModel<Livro>> pagedModel =
                pagedResourcesAssembler.toModel(page, assembler);

        return ResponseEntity.ok()
                .header("X-API-Version", "1.0")
                .body(pagedModel);
    }

    // =====================================================
    // LISTAGEM V2
    // =====================================================

    @GetMapping(headers = "X-API-Version=2")
    @Operation(
            summary = "Listar livros (v2)",
            description = "Recupera a lista paginada de livros utilizando "
                    + "a estrutura estendida LivroV2DTO."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de livros v2 recuperada com sucesso",
                    headers = @Header(
                            name = "X-API-Version",
                            description = "Versão da API utilizada",
                            schema = @Schema(type = "string", example = "2.0")
                    )
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
    public ResponseEntity<PaginaResponseDTO<LivroV2DTO>> listarV2(
            @ParameterObject Pageable pageable) {

        PaginaResponseDTO<LivroV2DTO> pagina =
                PaginaResponseDTO.from(
                        livroRepository.findAll(pageable),
                        livro -> new LivroV2DTO(
                                livro,
                                "v2.0 - Resposta estendida com metadados"
                        )
                );

        return ResponseEntity.ok()
                .header("X-API-Version", "2.0")
                .body(pagina);
    }

    // =====================================================
    // BUSCAR POR TÍTULO
    // =====================================================

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar livros por título",
            description = "Consulta livros que contenham o termo informado no título."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Termo de busca ausente ou parâmetros de paginação inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<PagedModel<EntityModel<Livro>>> buscarPorTitulo(

            @Parameter(
                    description = "Termo ou título a ser pesquisado",
                    required = true,
                    example = "Dom"
            )
            @RequestParam(name = "titulo")
            @NotBlank(message = "O parâmetro 'titulo' é obrigatório.")
            String titulo,

            @ParameterObject Pageable pageable,

            @Parameter(hidden = true)
            PagedResourcesAssembler<Livro> pagedResourcesAssembler) {

        Page<Livro> page =
                livroRepository.findByTituloContainingIgnoreCase(
                        titulo,
                        pageable
                );

        PagedModel<EntityModel<Livro>> pagedModel =
                pagedResourcesAssembler.toModel(page, assembler);

        return ResponseEntity.ok(pagedModel);
    }

    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar livro por ID",
            description = "Retorna os detalhes de um livro específico pelo seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro localizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador inválido. O ID deve ser positivo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public EntityModel<Livro> buscarPorId(

            @Parameter(
                    description = "Identificador único do livro",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo")
            Long id) {

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        return assembler.toModel(livro);
    }

    // =====================================================
    // CADASTRAR LIVRO
    // =====================================================

    @PostMapping
    @Operation(
            summary = "Cadastrar novo livro",
            description = "Cadastra um novo livro associando autores existentes. "
                    + "Suporta idempotência por meio do cabeçalho X-Idempotency-Key."
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
                    responseCode = "201",
                    description = "Livro cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos ou ano de publicação futuro",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Um ou mais autores informados não foram encontrados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "ISBN duplicado, conflito de integridade ou requisição idempotente ainda em processamento",
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
                    description = "X-Idempotency-Key reutilizada com método, rota ou corpo diferente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public ResponseEntity<EntityModel<Livro>> cadastrar(

            @RequestBody(
                    description = "Dados do livro para cadastro",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = LivroRequestDTO.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "titulo": "Dom Casmurro",
                                              "isbn": "9788535910663",
                                              "anoPublicacao": 1899,
                                              "autores": [
                                                { "id": 1 }
                                              ]
                                            }
                                            """
                            )
                    )
            )
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            LivroRequestDTO dto,

            @Parameter(
                    description = "Chave única do cliente para idempotência. "
                            + "Reutilize a mesma chave somente com o mesmo método, rota e corpo.",
                    example = "cadastro-0001"
            )
            @RequestHeader(
                    value = "X-Idempotency-Key",
                    required = false
            )
            String idempotencyKey) {

        Livro livro = dto.toEntity(resolverAutores(dto.autores()));

        if (livro.getAnoPublicacao() != null
                && livro.getAnoPublicacao() > LocalDate.now().getYear()) {

            throw new RequisicaoInvalidaException(
                    "O ano de publicação não pode ser maior que o ano atual."
            );
        }

        Livro novoLivro = livroRepository.save(livro);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(novoLivro));
    }

    // =====================================================
    // ATUALIZAR LIVRO
    // =====================================================

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar livro existente",
            description = "Atualiza os dados de um livro já cadastrado."
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
                    responseCode = "200",
                    description = "Livro atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de atualização inválidos ou ano de publicação futuro",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro ou autor informado não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "ISBN duplicado ou conflito de integridade de dados",
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
    public EntityModel<Livro> atualizar(

            @Parameter(
                    description = "Identificador único do livro",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo")
            Long id,

            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            LivroRequestDTO dto) {

        Livro livro = dto.toEntity(resolverAutores(dto.autores()));

        Livro existente = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        if (livro.getAnoPublicacao() != null
                && livro.getAnoPublicacao() > LocalDate.now().getYear()) {

            throw new RequisicaoInvalidaException(
                    "O ano de publicação não pode ser maior que o ano atual."
            );
        }

        existente.setTitulo(livro.getTitulo());
        existente.setIsbn(livro.getIsbn());
        existente.setAnoPublicacao(livro.getAnoPublicacao());
        existente.setCapaUrl(livro.getCapaUrl());

        if (livro.getAutores() != null && !livro.getAutores().isEmpty()) {
            existente.setAutores(livro.getAutores());
        }

        Livro livroAtualizado = livroRepository.save(existente);

        return assembler.toModel(livroAtualizado);
    }

    // =====================================================
    // EXCLUIR LIVRO
    // =====================================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir livro",
            description = "Remove um livro do acervo da biblioteca."
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
                    responseCode = "204",
                    description = "Livro excluído com sucesso. A resposta não possui corpo."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador inválido. O ID deve ser positivo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não localizado pelo ID informado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de integridade: não é possível excluir livro vinculado a empréstimos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponse.class)
                    )
            )
    })
    public void excluir(

            @Parameter(
                    description = "Identificador único do livro a ser excluído",
                    example = "1"
            )
            @PathVariable
            @Positive(message = "O id deve ser positivo")
            Long id) {

        Livro existente = livroRepository.findById(id)
                .orElseThrow(() -> new LivroNotFoundException(id));

        livroRepository.delete(existente);
    }

    // =====================================================
    // RESOLVER AUTORES
    // =====================================================

    private Set<Autor> resolverAutores(List<ReferenciaDTO> referencias) {

        Set<Long> ids = referencias.stream()
                .map(ReferenciaDTO::id)
                .collect(Collectors.toCollection(HashSet::new));

        List<Autor> encontrados = autorRepository.findAllById(ids);

        if (encontrados.size() != ids.size()) {

            Set<Long> achados = encontrados.stream()
                    .map(Autor::getId)
                    .collect(Collectors.toSet());

            ids.removeAll(achados);

            throw new AutorNotFoundException(ids.iterator().next());
        }

        return new HashSet<>(encontrados);
    }
}
