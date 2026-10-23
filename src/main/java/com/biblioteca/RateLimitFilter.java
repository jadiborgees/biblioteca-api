package com.biblioteca;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private static final int CAPACITY = 30; // Capacidade máxima do balde
    private static final double REFILL_RATE_PER_SEC = 30.0 / 60.0; // 30 tokens por minuto (0.5 por segundo)

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = httpRequest.getRemoteAddr();

        buckets.putIfAbsent(clientIp, new TokenBucket(CAPACITY, REFILL_RATE_PER_SEC));
        TokenBucket bucket = buckets.get(clientIp);

        if (!bucket.tryConsume()) {
            int segundosEspera = bucket.getSecondsToNextToken();

            httpResponse.setStatus(429); // Código HTTP 429: Too Many Requests
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(String.format(
                    "{\"erro\": \"Limite de requisicoes excedido. Tente novamente em %d segundos.\"}",
                    segundosEspera
            ));
            return;
        }

        chain.doFilter(request, response);
    }

    private static class TokenBucket {
        private final double capacity;
        private final double refillRatePerSec;
        private double tokens;
        private long lastRefillTimestamp;

        public TokenBucket(double capacity, double refillRatePerSec) {
            this.capacity = capacity;
            this.refillRatePerSec = refillRatePerSec;
            this.tokens = capacity; // Começa com o balde cheio
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        public synchronized int getSecondsToNextToken() {
            refill();
            if (tokens >= 1.0) {
                return 0;
            }
            double deficit = 1.0 - tokens;
            double seconds = deficit / refillRatePerSec;
            return (int) Math.ceil(seconds);
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastRefillTimestamp;
            if (elapsedTime > 0) {
                lastRefillTimestamp = now;
                double tokensToAdd = (elapsedTime / 1000.0) * refillRatePerSec;
                tokens = Math.min(capacity, tokens + tokensToAdd);
            }
        }
    }
}