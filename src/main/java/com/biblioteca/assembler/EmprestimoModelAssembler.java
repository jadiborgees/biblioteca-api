package com.biblioteca.assembler;

import com.biblioteca.controller.EmprestimoController;
import com.biblioteca.model.Emprestimo;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EmprestimoModelAssembler
        implements RepresentationModelAssembler<Emprestimo, EntityModel<Emprestimo>> {

    @Override
    public EntityModel<Emprestimo> toModel(Emprestimo emprestimo) {

        return EntityModel.of(
                emprestimo,

                // Link para o próprio empréstimo.
                linkTo(
                        methodOn(EmprestimoController.class)
                                .buscarPorId(emprestimo.getId())
                ).withSelfRel(),

                // Link para a lista de empréstimos.
                linkTo(
                        methodOn(EmprestimoController.class)
                                .listar(0, 2)
                ).withRel("emprestimos"),

                // Link para registrar a devolução.
                linkTo(
                        methodOn(EmprestimoController.class)
                                .devolver(emprestimo.getId())
                ).withRel("devolver")
        );
    }
}