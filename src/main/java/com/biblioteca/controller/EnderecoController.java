package com.biblioteca.controller;

import com.biblioteca.assembler.EnderecoModelAssembler;
import com.biblioteca.exception.EnderecoNotFoundException;
import com.biblioteca.model.Endereco;
import com.biblioteca.repository.EnderecoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
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
        description = "Endpoints para cadastro, consulta, atualização e exclusão de endereços"
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


    // LISTAR ENDEREÇOS
    @GetMapping
    @Operation(
            summary = "Listar endereços",
            description = "Retorna todos os endereços cadastrados de forma paginada."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Endereços listados com sucesso"
    )
    public PagedModel<EntityModel<Endereco>> listar(

            @Parameter(
                    description = "Número da página, iniciando em 0",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Quantidade de registros por página",
                    example = "2"
            )
            @RequestParam(defaultValue = "2") int size,

            PagedResourcesAssembler<Endereco> pagedAssembler) {

        Pageable pageable = PageRequest.of(page, size);

        return pagedAssembler.toModel(
                enderecoRepository.findAll(pageable),
                assembler
        );
    }


    // BUSCAR ENDEREÇOS POR CIDADE
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar endereços por cidade",
            description = "Busca endereços que contenham o texto informado no nome da cidade. A busca não diferencia letras maiúsculas e minúsculas e o resultado é paginado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetro de busca ou paginação inválido"
            )
    })
    public PagedModel<EntityModel<Endereco>> buscarPorCidade(

            @Parameter(
                    description = "Cidade completa ou parte do nome da cidade",
                    example = "São Paulo"
            )
            @RequestParam String cidade,

            @Parameter(
                    description = "Número da página, iniciando em 0",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Quantidade de registros por página",
                    example = "2"
            )
            @RequestParam(defaultValue = "2") int size,

            PagedResourcesAssembler<Endereco> pagedAssembler) {

        Pageable pageable = PageRequest.of(page, size);

        return pagedAssembler.toModel(
                enderecoRepository.findByCidadeContainingIgnoreCase(
                        cidade,
                        pageable
                ),
                assembler
        );
    }


    // BUSCAR ENDEREÇO POR ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar endereço por ID",
            description = "Retorna os dados de um endereço específico a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Endereço encontrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não encontrado"
            )
    })
    public EntityModel<Endereco> buscarPorId(

            @Parameter(
                    description = "ID do endereço",
                    example = "1"
            )
            @PathVariable Long id) {

        Endereco endereco = enderecoRepository
                .findById(id)
                .orElseThrow(() ->
                        new EnderecoNotFoundException(id)
                );

        return assembler.toModel(endereco);
    }


    // CADASTRAR ENDEREÇO
    @PostMapping
    @Operation(
            summary = "Cadastrar endereço",
            description = "Cadastra um novo endereço."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do endereço que será cadastrado",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "rua": "Rua das Flores",
                                      "numero": "120",
                                      "bairro": "Centro",
                                      "cidade": "São Paulo",
                                      "estado": "SP",
                                      "cep": "01001-000"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Endereço cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do endereço inválidos"
            )
    })
    public ResponseEntity<EntityModel<Endereco>> cadastrar(
            @Valid @RequestBody Endereco endereco) {

        Endereco novoEndereco = enderecoRepository.save(endereco);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(novoEndereco));
    }


    // ATUALIZAR ENDEREÇO
    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar endereço",
            description = "Atualiza rua, número, bairro, cidade, estado e CEP de um endereço existente."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do endereço",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "rua": "Avenida Paulista",
                                      "numero": "1000",
                                      "bairro": "Bela Vista",
                                      "cidade": "São Paulo",
                                      "estado": "SP",
                                      "cep": "01310-100"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Endereço atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do endereço inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não encontrado"
            )
    })
    public EntityModel<Endereco> atualizar(

            @Parameter(
                    description = "ID do endereço que será atualizado",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid @RequestBody Endereco endereco) {

        Endereco existente = enderecoRepository
                .findById(id)
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


    // EXCLUIR ENDEREÇO
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Excluir endereço",
            description = "Exclui permanentemente um endereço cadastrado a partir do seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Endereço excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não encontrado"
            )
    })
    public void excluir(

            @Parameter(
                    description = "ID do endereço que será excluído",
                    example = "1"
            )
            @PathVariable Long id) {

        if (!enderecoRepository.existsById(id)) {
            throw new EnderecoNotFoundException(id);
        }

        enderecoRepository.deleteById(id);
    }
}