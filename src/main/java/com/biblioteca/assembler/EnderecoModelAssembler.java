package com.biblioteca.assembler;

import com.biblioteca.controller.EnderecoController;
import com.biblioteca.model.Endereco;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EnderecoModelAssembler
        implements RepresentationModelAssembler<Endereco, EntityModel<Endereco>> {

    @Override
    public EntityModel<Endereco> toModel(Endereco endereco) {

        return EntityModel.of(
                endereco,

                // Link para o próprio endereço
                linkTo(methodOn(EnderecoController.class)
                        .buscarPorId(endereco.getId()))
                        .withSelfRel(),

                // Link para a lista de endereços
                linkTo(EnderecoController.class)
                        .withRel("enderecos"),

                // Link para atualizar o endereço
                linkTo(methodOn(EnderecoController.class)
                        .atualizar(endereco.getId(), null))
                        .withRel("atualizar"),

                // Link para excluir o endereço
                linkTo(EnderecoController.class)
                        .slash(endereco.getId())
                        .withRel("excluir")
        );
    }
}