package com.biblioteca.assembler;

import com.biblioteca.controller.AutorController;
import com.biblioteca.controller.LivroController;
import com.biblioteca.model.Autor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class AutorModelAssembler implements RepresentationModelAssembler<Autor, EntityModel<Autor>> {

    @Override
    public EntityModel<Autor> toModel(Autor autor) {
        Link selfLink = linkTo(methodOn(AutorController.class).buscarPorId(autor.getId())).withSelfRel();

        return EntityModel.of(autor,
                selfLink,
                Link.of(selfLink.getHref()).withRel("update"),
                Link.of(selfLink.getHref()).withRel("delete"),
                linkTo(methodOn(AutorController.class).listar(null, null)).withRel("autores"),
                linkTo(methodOn(LivroController.class).listar(null, null)).withRel("livros")
        );
    }
}