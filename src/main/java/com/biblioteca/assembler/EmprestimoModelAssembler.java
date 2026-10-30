package com.biblioteca.assembler;

import com.biblioteca.controller.EmprestimoController;
import com.biblioteca.model.Emprestimo;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EmprestimoModelAssembler implements RepresentationModelAssembler<Emprestimo, EntityModel<Emprestimo>> {

    @Override
    public EntityModel<Emprestimo> toModel(Emprestimo emprestimo) {
        Link selfLink = linkTo(methodOn(EmprestimoController.class).buscarPorId(emprestimo.getId())).withSelfRel();

        return EntityModel.of(emprestimo,
                selfLink,
                Link.of(selfLink.getHref()).withRel("update"),
                Link.of(selfLink.getHref()).withRel("delete"),
                linkTo(methodOn(EmprestimoController.class).devolver(emprestimo.getId())).withRel("devolver"),
                linkTo(methodOn(EmprestimoController.class).listar(null, null)).withRel("emprestimos")
        );
    }
}