package com.biblioteca.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Estrutura de paginação estável para respostas que não usam HATEOAS (ex.: versão 2 de /livros). */
public record PaginaResponseDTO<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas
) {
    public static <E, T> PaginaResponseDTO<T> from(Page<E> page, Function<E, T> mapeador) {
        return new PaginaResponseDTO<>(
                page.getContent().stream().map(mapeador).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
