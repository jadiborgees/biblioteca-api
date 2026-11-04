package com.biblioteca.dto;

import com.biblioteca.model.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequestDTO(
        @NotBlank(message = "O logradouro é obrigatório.")
        String logradouro,

        @NotBlank(message = "O número é obrigatório.")
        String numero,

        String complemento,

        @NotBlank(message = "O bairro é obrigatório.")
        String bairro,

        @NotBlank(message = "A cidade é obrigatória.")
        String cidade,

        @NotBlank(message = "O estado é obrigatório.")
        @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 letras (ex: SP).")
        String estado,

        @NotBlank(message = "O CEP é obrigatório.")
        @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "O CEP deve estar no formato 00000-000.")
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
