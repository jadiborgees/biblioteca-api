package com.biblioteca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class ApiVersionFilter extends OncePerRequestFilter {

    private static final Set<String> SUPPORTED_VERSIONS = Set.of("1", "2", "v1", "v2");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Libera documentação e rotas públicas
        if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiVersion = request.getHeader("X-API-Version");

        // Se a versão for enviada e não for suportada, retorna 400 Bad Request
        if (apiVersion != null && !apiVersion.isBlank()) {
            if (!SUPPORTED_VERSIONS.contains(apiVersion.trim().toLowerCase())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(String.format(
                        "{\"status\": 400, \"erro\": \"Versão de API Não Suportada\", \"mensagem\": \"A versão '%s' não é suportada. Versões disponíveis: v1, v2.\"}",
                        apiVersion
                ));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}