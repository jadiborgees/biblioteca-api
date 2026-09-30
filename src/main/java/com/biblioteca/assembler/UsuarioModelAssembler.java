package com.biblioteca.assembler;

import com.biblioteca.controller.UsuarioController;
import com.biblioteca.model.Usuario;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UsuarioModelAssembler
        implements RepresentationModelAssembler<Usuario, EntityModel<Usuario>> {

    @Override
    public EntityModel<Usuario> toModel(Usuario usuario) {

        return EntityModel.of(
                usuario,

                // Link para o próprio usuário
                linkTo(methodOn(UsuarioController.class)
                        .buscarPorId(usuario.getId()))
                        .withSelfRel(),

                // Link para a lista de usuários
                linkTo(methodOn(UsuarioController.class)
                        .listar(null, null))
                        .withRel("usuarios"),

                // Link para atualizar o usuário
                linkTo(methodOn(UsuarioController.class)
                        .atualizar(usuario.getId(), null))
                        .withRel("atualizar"),

                // Link para excluir o usuário
                linkTo(UsuarioController.class)
                        .slash(usuario.getId())
                        .withRel("excluir")
        );
    }
}