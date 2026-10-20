package com.biblioteca.assembler;

import com.biblioteca.controller.EnderecoController;
import com.biblioteca.controller.EmprestimoController;
import com.biblioteca.controller.UsuarioController;
import com.biblioteca.model.Usuario;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<Usuario, EntityModel<Usuario>> {

    @Override
    public EntityModel<Usuario> toModel(Usuario usuario) {
        Link selfLink = linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.getId())).withSelfRel();

        EntityModel<Usuario> usuarioModel = EntityModel.of(usuario,
                selfLink,
                Link.of(selfLink.getHref()).withRel("update"),
                Link.of(selfLink.getHref()).withRel("delete"),
                linkTo(methodOn(UsuarioController.class).listar(null, null)).withRel("usuarios")
        );

        if (usuario.getEndereco() != null) {
            usuarioModel.add(linkTo(methodOn(EnderecoController.class).buscarPorId(usuario.getEndereco().getId())).withRel("endereco"));
        }

        return usuarioModel;
    }
}