package com.biblioteca.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static class CachedResponse {
        int status;
        byte[] body;
        String contentType;

        CachedResponse(int status, byte[] body, String contentType) {
            this.status = status;
            this.body = body;
            this.contentType = contentType;
        }
    }

    private final Map<String, CachedResponse> responseCache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Aplica idempotência apenas para métodos de escrita que alteram estado (POST e PUT)
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader("X-Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Se a chave já foi processada com SUCESSO anterior, faz o REPLAY da resposta
        if (responseCache.containsKey(idempotencyKey)) {
            CachedResponse cached = responseCache.get(idempotencyKey);
            response.setStatus(cached.status);
            if (cached.contentType != null) {
                response.setContentType(cached.contentType);
            }
            response.setHeader("X-Cache-Lookup", "HIT - Idempotent Replay");
            response.getOutputStream().write(cached.body);
            return;
        }

        // 2. Envolve a resposta num wrapper para capturar o corpo e o status
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, responseWrapper);

            int status = responseWrapper.getStatus();

            // 3. Apenas guarda no cache se o processamento foi bem-sucedido (status 2xx)
            if (status >= 200 && status < 300) {
                byte[] body = responseWrapper.getContentAsByteArray();
                responseCache.put(idempotencyKey, new CachedResponse(status, body, responseWrapper.getContentType()));
            }

            // 4. Copia o conteúdo de volta para o cliente
            responseWrapper.copyBodyToResponse();

        } catch (Exception e) {
            // Em caso de erro na execução, não armazena no cache para permitir nova tentativa
            throw e;
        }
    }
}