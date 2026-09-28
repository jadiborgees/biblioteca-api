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

                linkTo(methodOn(UsuarioController.class)
                        .buscarPorId(usuario.getId()))
                        .withSelfRel(),

                linkTo(methodOn(UsuarioController.class)
                        .listar(null, null))
                        .withRel("usuarios")
        );
    }
}