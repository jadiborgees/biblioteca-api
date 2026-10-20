package com.biblioteca.assembler;

import com.biblioteca.controller.AutorController;
import com.biblioteca.controller.LivroController;
import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class LivroModelAssembler implements RepresentationModelAssembler<Livro, EntityModel<Livro>> {

    @Override
    public EntityModel<Livro> toModel(Livro livro) {
        Link selfLink = linkTo(methodOn(LivroController.class).buscar(livro.getId())).withSelfRel();

        EntityModel<Livro> livroModel = EntityModel.of(livro,
                selfLink,
                Link.of(selfLink.getHref()).withRel("update"),
                Link.of(selfLink.getHref()).withRel("delete"),
                linkTo(methodOn(LivroController.class).listar(null, null)).withRel("livros")
        );

        if (livro.getAutores() != null) {
            for (Autor autor : livro.getAutores()) {
                livroModel.add(linkTo(methodOn(AutorController.class).buscarPorId(autor.getId())).withRel("autores"));
            }
        }

        return livroModel;
    }
}