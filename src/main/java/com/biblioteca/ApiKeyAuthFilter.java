package com.biblioteca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Libera requisições Preflight CORS (método OPTIONS) sem exigir X-API-Key
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Libera rotas públicas (Swagger, H2 Console, etc.)
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
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/h2-console")
                || uri.equals("/api/keys/generate");
    }
}