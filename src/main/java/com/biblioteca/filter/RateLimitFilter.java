
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

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Entrada> baldes = new ConcurrentHashMap<>();
    private final AtomicLong acessos = new AtomicLong();
    private final Object lockLimpeza = new Object();

    private final long capacidade;
    private final Duration periodo;
    private final ErroHttpWriter erroHttpWriter;

    public RateLimitFilter(
            long capacidade,
            Duration periodo,
            ErroHttpWriter erroHttpWriter
    ) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException(
                    "A capacidade deve ser maior que zero."
            );
        }

        if (periodo == null || periodo.isZero() || periodo.isNegative()) {
            throw new IllegalArgumentException(
                    "O período deve ser maior que zero."
            );
        }

        this.capacidade = capacidade;
        this.periodo = periodo;
        this.erroHttpWriter = erroHttpWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String caminho = request.getRequestURI();

        return caminho.equals("/swagger-ui.html")
                || caminho.startsWith("/swagger-ui/")
                || caminho.equals("/swagger-ui")
                || caminho.startsWith("/v3/api-docs")
                || caminho.startsWith("/swagger-resources")
                || caminho.startsWith("/webjars/")
                || caminho.equals("/favicon.ico")
                || caminho.startsWith("/h2-console/")
                || caminho.equals("/termos-de-uso");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long agora = System.currentTimeMillis();

        String ipCliente = obterIpCliente(request);

        Entrada entrada;

        synchronized (lockLimpeza) {
            entrada = baldes.computeIfAbsent(
                    ipCliente,
                    ip -> new Entrada(criarBalde(), agora)
            );

            entrada.ultimoAcesso = agora;

            limparOciosos(agora);
        }

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

            response.setHeader(
                    "X-RateLimit-Reset",
                    String.valueOf(segundos(periodo.toNanos() / capacidade))
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
                "Limite de Requisições Excedido",
                "Limite de " + capacidade
                        + " requisições por "
                        + periodo.toSeconds()
                        + " segundos excedido. Tente novamente em "
                        + espera
                        + " segundo(s)."
        );
    }

    private String obterIpCliente(HttpServletRequest request) {

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String primeiroIp = forwardedFor.split(",")[0].trim();

            if (!primeiroIp.isBlank()) {
                return primeiroIp;
            }
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private Bucket criarBalde() {

        Bandwidth limite = Bandwidth.classic(
                capacidade,
                Refill.greedy(capacidade, periodo)
        );

        return Bucket.builder()
                .addLimit(limite)
                .build();
    }

    private void limparOciosos(long agora) {

        if (acessos.incrementAndGet() % 500 != 0) {
            return;
        }

        long limite = periodo.toMillis() * 2;

        baldes.entrySet().removeIf(
                item -> (agora - item.getValue().ultimoAcesso) > limite
        );
    }

    private static long segundos(long nanos) {
        return (long) Math.ceil(nanos / 1_000_000_000.0);
    }

    private static final class Entrada {

        private final Bucket balde;
        private volatile long ultimoAcesso;

        Entrada(Bucket balde, long ultimoAcesso) {
            this.balde = balde;
            this.ultimoAcesso = ultimoAcesso;
        }
    }
}
