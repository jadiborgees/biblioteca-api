
package com.biblioteca.dto;

import com.biblioteca.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(
        name = "UsuarioRequestDTO",
        description = "Dados necessários para cadastrar ou atualizar um usuário da biblioteca."
)
public record UsuarioRequestDTO(

        @Schema(
                description = "Nome completo do usuário.",
                example = "João da Silva",
                minLength = 2,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 100,
                message = "O nome deve ter entre 2 e 100 caracteres.")
        String nome,

        @Schema(
                description = "Endereço de e-mail do usuário. Deve possuir formato válido.",
                example = "joao.silva@email.com",
                format = "email",
                minLength = 5,
                maxLength = 254,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Forneça um endereço de e-mail válido.")
        @Size(min = 5, max = 254,
                message = "O e-mail deve ter entre 5 e 254 caracteres.")
        String email,

        @Schema(
                description = "Telefone de contato com DDD. Aceita formatos como (11) 99999-8888 ou 11999998888. Campo opcional.",
                example = "11999998888",
                pattern = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$",
                minLength = 10,
                maxLength = 15,
                nullable = true
        )
        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$",
                message = "O telefone deve ter um formato válido (ex: 11999998888)."
        )
        @Size(max = 15,
                message = "O telefone deve ter no máximo 15 caracteres.")
        String telefone,

        @Schema(
                description = "Data de nascimento do usuário. Deve ser anterior à data atual.",
                example = "1998-05-20",
                type = "string",
                format = "date",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve ser uma data no passado.")
        LocalDate dataNascimento,

        @Schema(
                description = "Endereço do usuário. Deve respeitar as validações definidas no EnderecoRequestDTO.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
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
