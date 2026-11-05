
package com.biblioteca.filter;

import com.biblioteca.exception.ErroHttpWriter;
import com.biblioteca.model.ApiKey;
import com.biblioteca.service.ApiKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Exige uma chave de API válida (X-API-Key)
 * nas rotas protegidas e aplica o nível de acesso.
 *
 * Públicas:
 * - POST /auth/login
 * - GET/HEAD de /livros e /autores
 * - Swagger, H2 console e documentação.
 *
 * Protegidas:
 * - Todas as demais rotas.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    public static final String ATRIBUTO_USUARIO = "apiKey.usuario";

    public static final String ATRIBUTO_NIVEL = "apiKey.nivel";

    private static final List<String> LEITURA_PUBLICA =
            List.of("/livros", "/autores");

    private static final List<String> ROTAS_PUBLICAS =
            List.of(
                    "/swagger-ui",
                    "/v3/api-docs",
                    "/h2-console",
                    "/error",
                    "/termos-de-uso"
            );

    private final ApiKeyService apiKeyService;

    private final ErroHttpWriter erroHttpWriter;

    public ApiKeyAuthFilter(
            ApiKeyService apiKeyService,
            ErroHttpWriter erroHttpWriter
    ) {
        this.apiKeyService = apiKeyService;
        this.erroHttpWriter = erroHttpWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        // Permite requisições de preflight CORS.
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || rotaPublica(
                request.getMethod(),
                request.getRequestURI()
        );
    }

    static boolean rotaPublica(String metodo, String caminho) {

        // Login: somente POST /auth/login.
        if ("POST".equalsIgnoreCase(metodo)
                && "/auth/login".equals(caminho)) {
            return true;
        }

        // Documentação e rotas públicas.
        if (ROTAS_PUBLICAS.stream().anyMatch(caminho::startsWith)) {
            return true;
        }

        // Leitura pública de livros e autores.
        boolean leitura =
                "GET".equalsIgnoreCase(metodo)
                        || "HEAD".equalsIgnoreCase(metodo);

        return leitura
                && LEITURA_PUBLICA.stream().anyMatch(caminho::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String chave = request.getHeader(HEADER);

        if (chave == null || chave.isBlank()) {

            erroHttpWriter.escrever(
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Não Autorizado",
                    "Cabeçalho X-API-Key é obrigatório para acessar este recurso."
            );

            return;
        }

        Optional<ApiKey> encontrada =
                apiKeyService.validar(chave.trim());

        if (encontrada.isEmpty()) {

            erroHttpWriter.escrever(
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Não Autorizado",
                    "Chave de API inválida ou inativa."
            );

            return;
        }

        ApiKey apiKey = encontrada.get();

        if (!apiKey.getNivel().permite(
                request.getMethod(),
                request.getRequestURI()
        )) {

            erroHttpWriter.escrever(
                    request,
                    response,
                    HttpStatus.FORBIDDEN,
                    "Acesso Negado",
                    "A chave de nível " + apiKey.getNivel()
                            + " não tem permissão para esta operação."
            );

            return;
        }

        request.setAttribute(
                ATRIBUTO_USUARIO,
                apiKey.getUsuario()
        );

        request.setAttribute(
                ATRIBUTO_NIVEL,
                apiKey.getNivel().name()
        );

        filterChain.doFilter(request, response);
    }
}
