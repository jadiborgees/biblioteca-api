package com.biblioteca.filter;

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
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10; // Limite de requisições por janela
    private static final long WINDOW_DURATION_MS = 60_000; // Janela de 60 segundos (1 minuto)

    private static class RequestTracker {
        int count;
        long startTime;

        RequestTracker(long startTime) {
            this.count = 1;
            this.startTime = startTime;
        }
    }

    private final Map<String, RequestTracker> clientRequests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Identifica o cliente pelo IP ou chave de API
        String clientKey = request.getHeader("X-API-Key");
        if (clientKey == null || clientKey.isBlank()) {
            clientKey = request.getRemoteAddr();
        }

        long currentTime = System.currentTimeMillis();
        RequestTracker tracker = clientRequests.get(clientKey);

        if (tracker == null || (currentTime - tracker.startTime) > WINDOW_DURATION_MS) {
            // Nova janela de tempo
            tracker = new RequestTracker(currentTime);
            clientRequests.put(clientKey, tracker);
        } else {
            // Incrementa contador na janela atual
            tracker.count++;
        }

        long timeElapsed = currentTime - tracker.startTime;
        long timeRemainingMs = WINDOW_DURATION_MS - timeElapsed;
        long secondsUntilReset = Math.max(1, timeRemainingMs / 1000);

        int remainingRequests = Math.max(0, MAX_REQUESTS - tracker.count);

        // Define os cabeçalhos padrão de Rate Limit
        response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingRequests));
        response.setHeader("X-RateLimit-Reset", String.valueOf(secondsUntilReset));

        // Se excedeu o limite de requisições
        if (tracker.count > MAX_REQUESTS) {
            response.setStatus(429); // 429 Too Many Requests
            response.setHeader("Retry-After", String.valueOf(secondsUntilReset));
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                    "{\"erro\": \"Limite de requisições excedido. Tente novamente em %d segundos.\"}",
                    secondsUntilReset
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }
}