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

                linkTo(
                        methodOn(EnderecoController.class)
                                .buscarPorId(endereco.getId())
                ).withSelfRel(),

                linkTo(
                        methodOn(EnderecoController.class)
                                .listar(0, 2)
                ).withRel("enderecos")
        );
    }
}