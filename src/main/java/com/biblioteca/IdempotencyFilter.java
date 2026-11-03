package com.biblioteca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final Map<String, String> idempotencyKeys = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String idempotencyKey = request.getHeader("X-Idempotency-Key");

            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                if (idempotencyKeys.containsKey(idempotencyKey)) {
                    response.setStatus(409); // Conflict ou 200/400 dependendo da sua regra de negócio
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"erro\": \"Operação já processada com esta chave de idempotência.\"}");
                    return;
                }
                idempotencyKeys.put(idempotencyKey, "PROCESSED");
            }
        }

        filterChain.doFilter(request, response);
    }
}