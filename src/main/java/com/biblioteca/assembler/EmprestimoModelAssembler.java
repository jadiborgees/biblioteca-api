package com.biblioteca.assembler;

import com.biblioteca.controller.EmprestimoController;
import com.biblioteca.controller.LivroController;
import com.biblioteca.controller.UsuarioController;
import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.StatusEmprestimo;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EmprestimoModelAssembler implements RepresentationModelAssembler<Emprestimo, EntityModel<Emprestimo>> {

    @Override
    public EntityModel<Emprestimo> toModel(Emprestimo emprestimo) {
        Link selfLink = linkTo(methodOn(EmprestimoController.class).buscarPorId(emprestimo.getId())).withSelfRel();

        EntityModel<Emprestimo> emprestimoModel = EntityModel.of(emprestimo,
                selfLink,
                Link.of(selfLink.getHref()).withRel("update"),
                Link.of(selfLink.getHref()).withRel("delete"),
                linkTo(methodOn(EmprestimoController.class).listar(null, null)).withRel("emprestimos")
        );

        if (emprestimo.getLivro() != null) {
            emprestimoModel.add(linkTo(methodOn(LivroController.class).buscar(emprestimo.getLivro().getId())).withRel("livro"));
        }

        if (emprestimo.getUsuario() != null) {
            emprestimoModel.add(linkTo(methodOn(UsuarioController.class).buscarPorId(emprestimo.getUsuario().getId())).withRel("usuario"));
        }

        if (emprestimo.getStatus() != StatusEmprestimo.DEVOLVIDO) {
            emprestimoModel.add(linkTo(methodOn(EmprestimoController.class).devolver(emprestimo.getId())).withRel("devolver"));
        }

        return emprestimoModel;
    }
}