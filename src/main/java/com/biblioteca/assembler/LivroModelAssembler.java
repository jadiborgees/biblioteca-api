package com.biblioteca.assembler;

import com.biblioteca.controller.LivroController;
import com.biblioteca.model.Livro;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class LivroModelAssembler implements RepresentationModelAssembler<Livro, EntityModel<Livro>> {

    @Override
    public EntityModel<Livro> toModel(Livro livro) {
        Link selfLink = linkTo(methodOn(LivroController.class).buscarPorId(livro.getId())).withSelfRel();

        return EntityModel.of(livro,
                selfLink,
                Link.of(selfLink.getHref()).withRel("update"),
                Link.of(selfLink.getHref()).withRel("delete"),
                linkTo(methodOn(LivroController.class).listar(null, null)).withRel("livros")
        );
    }
}