
package com.biblioteca.filter;

import com.biblioteca.exception.ErroHttpWriter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Limite de requisicoes por cliente (IP), com token bucket (Bucket4j).
 *
 * Cada cliente possui uma capacidade de fichas, reabastecidas
 * continuamente ao longo do periodo configurado.
 *
 * Ao exceder, responde 429 com Retry-After.
 * Baldes ociosos sao removidos periodicamente.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Entrada> baldes = new ConcurrentHashMap<>();
    private final AtomicLong acessos = new AtomicLong();

    private final long capacidade;
    private final Duration periodo;
    private final ErroHttpWriter erroHttpWriter;

    public RateLimitFilter(
            long capacidade,
            Duration periodo,
            ErroHttpWriter erroHttpWriter
    ) {
        this.capacidade = capacidade;
        this.periodo = periodo;
        this.erroHttpWriter = erroHttpWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long agora = System.currentTimeMillis();

        // Diagnostico temporario para verificar o IP recebido pelo Render.
        System.out.println(
                "[RATE LIMIT] IP remoto: " + request.getRemoteAddr()
                        + " | X-Forwarded-For: "
                        + request.getHeader("X-Forwarded-For")
        );

        String ip = request.getRemoteAddr();

        Entrada entrada = baldes.computeIfAbsent(
                ip,
                chave -> new Entrada(criarBalde())
        );

        entrada.ultimoAcesso = agora;

        limparOciosos(agora);

        ConsumptionProbe probe =
                entrada.balde.tryConsumeAndReturnRemaining(1);

        long restantes = probe.getRemainingTokens();

        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(capacidade)
        );

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(restantes)
        );

        if (probe.isConsumed()) {

            long usadas = capacidade - restantes;

            response.setHeader(
                    "X-RateLimit-Reset",
                    String.valueOf(
                            segundos(
                                    usadas * periodo.toNanos() / capacidade
                            )
                    )
            );

            filterChain.doFilter(request, response);
            return;
        }

        long espera = Math.max(
                1,
                segundos(probe.getNanosToWaitForRefill())
        );

        response.setHeader(
                "Retry-After",
                String.valueOf(espera)
        );

        response.setHeader(
                "X-RateLimit-Reset",
                String.valueOf(espera)
        );

        erroHttpWriter.escrever(
                request,
                response,
                HttpStatus.TOO_MANY_REQUESTS,
                "Limite de Requisicoes Excedido",
                "Limite de " + capacidade
                        + " requisicoes por "
                        + periodo.toSeconds()
                        + " segundos excedido. Tente novamente em "
                        + espera + " segundo(s)."
        );
    }

    private Bucket criarBalde() {

        return Bucket.builder()
                .addLimit(
                        Bandwidth.classic(
                                capacidade,
                                Refill.greedy(capacidade, periodo)
                        )
                )
                .build();
    }

    private void limparOciosos(long agora) {

        if (acessos.incrementAndGet() % 500 == 0) {

            long limite = periodo.toMillis() * 2;

            baldes.values().removeIf(
                    entrada -> (agora - entrada.ultimoAcesso) > limite
            );
        }
    }

    private static long segundos(long nanos) {
        return (long) Math.ceil(nanos / 1_000_000_000.0);
    }

    private static final class Entrada {

        private final Bucket balde;
        private volatile long ultimoAcesso;

        Entrada(Bucket balde) {
            this.balde = balde;
            this.ultimoAcesso = System.currentTimeMillis();
        }
    }
}
