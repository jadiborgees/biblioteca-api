package com.biblioteca.config;

import com.biblioteca.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String caminho = request.getRequestURI();

// Rotas públicas e requisições CORS
        if (request.getMethod().equalsIgnoreCase("OPTIONS")
                || caminho.startsWith("/api-keys")
                || caminho.startsWith("/swagger-ui")
                || caminho.startsWith("/v3/api-docs")
                || caminho.startsWith("/h2-console")) {

            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");

        // API Key não enviada
        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"erro\":\"X-API-Key não informada\"}"
            );
            return;
        }

        // Verifica se existe e está ativa
        boolean chaveValida =
                apiKeyRepository.findByChaveAndAtivaTrue(apiKey).isPresent();

        if (!chaveValida) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"erro\":\"X-API-Key inválida ou inativa\"}"
            );
            return;
        }

        // API Key válida
        filterChain.doFilter(request, response);
    }
}