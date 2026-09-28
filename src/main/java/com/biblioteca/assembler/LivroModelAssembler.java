package com.biblioteca.assembler;

import com.biblioteca.controller.LivroController;
import com.biblioteca.model.Livro;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class LivroModelAssembler
        implements RepresentationModelAssembler<Livro, EntityModel<Livro>> {

    @Override
    public EntityModel<Livro> toModel(Livro livro) {

        return EntityModel.of(
                livro,

                linkTo(methodOn(LivroController.class)
                        .buscar(livro.getId()))
                        .withSelfRel(),

                linkTo(methodOn(LivroController.class)
                        .listar(null, null))
                        .withRel("livros")
        );
    }
}