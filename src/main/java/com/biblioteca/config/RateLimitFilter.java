package com.biblioteca.config;

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

    // Máximo de requisições permitidas dentro do período.
    private static final int LIMITE_REQUISICOES = 10;

    // Período de 1 minuto.
    private static final long PERIODO_MILISSEGUNDOS = 60_000;

    // Guarda o controle de requisições de cada cliente.
    private final Map<String, ControleRequisicoes> clientes =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Identifica o cliente pelo IP.
        String cliente = request.getRemoteAddr();

        long agora = System.currentTimeMillis();

        ControleRequisicoes controle = clientes.computeIfAbsent(
                cliente,
                chave -> new ControleRequisicoes(agora)
        );

        synchronized (controle) {

            // Se passou 1 minuto, reinicia o contador.
            if (agora - controle.inicioPeriodo >= PERIODO_MILISSEGUNDOS) {
                controle.inicioPeriodo = agora;
                controle.quantidade = 0;
            }

            // Se ultrapassou o limite, retorna 429.
            if (controle.quantidade >= LIMITE_REQUISICOES) {

                long tempoRestante =
                        PERIODO_MILISSEGUNDOS - (agora - controle.inicioPeriodo);

                long segundosRestantes =
                        Math.max(1, (tempoRestante + 999) / 1000);

                response.setStatus(429);
                response.setHeader(
                        "Retry-After",
                        String.valueOf(segundosRestantes)
                );
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"erro\":\"Limite de requisições excedido. Tente novamente mais tarde.\"}"
                );

                return;
            }

            // Conta a requisição atual.
            controle.quantidade++;
        }

        filterChain.doFilter(request, response);
    }

    private static class ControleRequisicoes {

        private long inicioPeriodo;
        private int quantidade;

        public ControleRequisicoes(long inicioPeriodo) {
            this.inicioPeriodo = inicioPeriodo;
            this.quantidade = 0;
        }
    }
}