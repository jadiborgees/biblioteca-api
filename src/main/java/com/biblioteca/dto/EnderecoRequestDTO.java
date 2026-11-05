
package com.biblioteca.dto;

import com.biblioteca.model.Endereco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
        name = "EnderecoRequestDTO",
        description = "Dados necessários para cadastrar ou atualizar um endereço."
)
public record EnderecoRequestDTO(

        @Schema(
                description = "Nome da rua, avenida, travessa ou outro tipo de logradouro.",
                example = "Avenida Paulista",
                minLength = 2,
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O logradouro é obrigatório.")
        @Size(min = 2, max = 150,
                message = "O logradouro deve ter entre 2 e 150 caracteres.")
        String logradouro,

        @Schema(
                description = "Número do imóvel. Aceita números e complementos como S/N.",
                example = "123",
                minLength = 1,
                maxLength = 20,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O número é obrigatório.")
        @Size(min = 1, max = 20,
                message = "O número deve ter entre 1 e 20 caracteres.")
        String numero,

        @Schema(
                description = "Complemento do endereço, como apartamento, bloco ou sala. Campo opcional.",
                example = "Apartamento 42, Bloco B",
                maxLength = 100,
                nullable = true
        )
        @Size(max = 100,
                message = "O complemento deve ter no máximo 100 caracteres.")
        String complemento,

        @Schema(
                description = "Bairro onde o imóvel está localizado.",
                example = "Bela Vista",
                minLength = 2,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O bairro é obrigatório.")
        @Size(min = 2, max = 100,
                message = "O bairro deve ter entre 2 e 100 caracteres.")
        String bairro,

        @Schema(
                description = "Nome do município onde o imóvel está localizado.",
                example = "São Paulo",
                minLength = 2,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "A cidade é obrigatória.")
        @Size(min = 2, max = 100,
                message = "A cidade deve ter entre 2 e 100 caracteres.")
        String cidade,

        @Schema(
                description = "Sigla da unidade federativa brasileira, com exatamente duas letras.",
                example = "SP",
                minLength = 2,
                maxLength = 2,
                pattern = "^[A-Za-z]{2}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O estado é obrigatório.")
        @Size(min = 2, max = 2,
                message = "O estado deve ter exatamente 2 letras (ex: SP).")
        @Pattern(regexp = "^[A-Za-z]{2}$",
                message = "O estado deve conter exatamente 2 letras.")
        String estado,

        @Schema(
                description = "Código de Endereçamento Postal (CEP). Aceita os formatos 00000-000 ou 00000000.",
                example = "01310-100",
                pattern = "^\\d{5}-?\\d{3}$",
                minLength = 8,
                maxLength = 9,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O CEP é obrigatório.")
        @Pattern(
                regexp = "^\\d{5}-?\\d{3}$",
                message = "O CEP deve estar no formato 00000-000."
        )
        String cep

) {

    public Endereco toEntity() {
        Endereco endereco = new Endereco();
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero);
        endereco.setComplemento(complemento);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setEstado(estado);
        endereco.setCep(cep);
        return endereco;
    }
}
