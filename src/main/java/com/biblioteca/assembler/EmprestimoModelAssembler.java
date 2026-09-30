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

                // Link para o próprio empréstimo
                linkTo(methodOn(EmprestimoController.class)
                        .buscarPorId(emprestimo.getId()))
                        .withSelfRel(),

                // Link para a lista de empréstimos
                linkTo(EmprestimoController.class)
                        .withRel("emprestimos"),

                // Link para atualizar o empréstimo
                linkTo(methodOn(EmprestimoController.class)
                        .atualizar(emprestimo.getId(), null))
                        .withRel("atualizar"),

                // Link para excluir o empréstimo
                linkTo(EmprestimoController.class)
                        .slash(emprestimo.getId())
                        .withRel("excluir"),

                // Link para registrar a devolução
                linkTo(methodOn(EmprestimoController.class)
                        .devolver(emprestimo.getId()))
                        .withRel("devolver")
        );
    }
}