
package com.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(
        name = "PaginaResponseDTO",
        description = "Estrutura padronizada de paginação para respostas da API que não utilizam HATEOAS."
)
public record PaginaResponseDTO<T>(

        @Schema(
                description = "Lista de registros pertencentes à página atual.",
                example = "[]",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        List<T> conteudo,

        @Schema(
                description = "Número da página atual. A contagem começa em zero.",
                example = "0",
                minimum = "0",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        int pagina,

        @Schema(
                description = "Quantidade de registros solicitados por página.",
                example = "10",
                minimum = "1",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        int tamanho,

        @Schema(
                description = "Quantidade total de registros disponíveis, considerando todas as páginas.",
                example = "35",
                minimum = "0",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        long totalElementos,

        @Schema(
                description = "Quantidade total de páginas disponíveis.",
                example = "4",
                minimum = "0",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        int totalPaginas

) {

    public static <E, T> PaginaResponseDTO<T> from(
            Page<E> page,
            Function<E, T> mapeador) {

        return new PaginaResponseDTO<>(
                page.getContent().stream().map(mapeador).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
