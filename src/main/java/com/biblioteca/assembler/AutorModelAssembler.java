package com.biblioteca.assembler;

import com.biblioteca.controller.AutorController;
import com.biblioteca.model.Autor;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class AutorModelAssembler
        implements RepresentationModelAssembler<Autor, EntityModel<Autor>> {

    @Override
    public EntityModel<Autor> toModel(Autor autor) {

        return EntityModel.of(
                autor,

                // Link para o próprio autor
                linkTo(methodOn(AutorController.class)
                        .buscarPorId(autor.getId()))
                        .withSelfRel(),

                // Link para a lista de autores
                linkTo(methodOn(AutorController.class)
                        .listar(null, null))
                        .withRel("autores"),

                // Link para atualizar o autor
                linkTo(methodOn(AutorController.class)
                        .atualizar(autor.getId(), null))
                        .withRel("atualizar"),

                // Link para excluir o autor
                linkTo(AutorController.class)
                        .slash(autor.getId())
                        .withRel("excluir")
        );
    }
}