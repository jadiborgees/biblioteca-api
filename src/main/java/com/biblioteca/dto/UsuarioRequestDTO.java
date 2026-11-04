package com.biblioteca.dto;

import com.biblioteca.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UsuarioRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Forneça um endereço de e-mail válido.")
        String email,

        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$",
                message = "O telefone deve ter um formato válido (ex: 11999998888).")
        String telefone,

        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve ser uma data no passado.")
        LocalDate dataNascimento,

        @Valid
        EnderecoRequestDTO endereco
) {
    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setDataNascimento(dataNascimento);
        usuario.setEndereco(endereco == null ? null : endereco.toEntity());
        return usuario;
    }
}
