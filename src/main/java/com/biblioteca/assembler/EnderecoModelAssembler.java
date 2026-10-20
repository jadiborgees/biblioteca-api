package com.biblioteca.assembler;

import com.biblioteca.controller.EnderecoController;
import com.biblioteca.model.Endereco;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EnderecoModelAssembler implements RepresentationModelAssembler<Endereco, EntityModel<Endereco>> {

    @Override
    public EntityModel<Endereco> toModel(Endereco endereco) {
        Link selfLink = linkTo(methodOn(EnderecoController.class).buscarPorId(endereco.getId())).withSelfRel();

        return EntityModel.of(endereco,
                selfLink,
                Link.of(selfLink.getHref()).withRel("update"),
                Link.of(selfLink.getHref()).withRel("delete"),
                linkTo(methodOn(EnderecoController.class).listar(null, null)).withRel("enderecos")
        );
    }
}