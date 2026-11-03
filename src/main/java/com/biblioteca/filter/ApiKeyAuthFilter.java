package com.biblioteca.filter;

import com.biblioteca.controller.ApiKeyController;
import com.biblioteca.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Libera requisições Preflight CORS (método OPTIONS) imediatamente
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return; // Retorna 200 OK diretamente para o navegador validar o CORS
        }

        // 2. Libera rotas públicas (Swagger, OpenAPI, H2 Console, Geração de Chaves)
        String requestURI = request.getRequestURI();
        if (isPublicRoute(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Validação do Cabeçalho X-API-Key
        String apiKeyHeader = request.getHeader("X-API-Key");

        if (apiKeyHeader == null || apiKeyHeader.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"erro\": \"Não autorizado: Cabeçalho X-API-Key ausente ou inválido.\"}");
            return;
        }

        // 4. Valida se a chave existe e está ativa no banco
        String keyHash = ApiKeyController.hashKey(apiKeyHeader);
        boolean isValidKey = apiKeyRepository.findByKeyHashAndActiveTrue(keyHash).isPresent();
        if (!isValidKey) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"erro\": \"Não autorizado: Chave de API inválida ou inativa.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicRoute(String uri) {
        return pathMatcher.match("/swagger-ui/**", uri)
                || pathMatcher.match("/swagger-ui.html", uri)
                || pathMatcher.match("/v3/api-docs/**", uri)
                || pathMatcher.match("/swagger-resources/**", uri)
                || pathMatcher.match("/h2-console/**", uri)
                || pathMatcher.match("/api/keys/generate", uri);
    }
}