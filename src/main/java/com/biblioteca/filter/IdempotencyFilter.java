package com.biblioteca.filter;

import com.biblioteca.exception.ErroHttpWriter;
import com.biblioteca.util.HashUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Idempotência para POST com cabeçalho X-Idempotency-Key.
 *
 * - Primeira requisição: processada. Se concluir com 2xx, a resposta fica guardada pelo TTL.
 * - Mesma chave, mesmo método/URI/corpo e já concluída: devolve a resposta original (Idempotent-Replayed: true).
 * - Mesma chave com método, URI ou corpo diferentes: 422.
 * - Mesma chave com a primeira requisição ainda em andamento: 409.
 * - Falhas (4xx/5xx) não ficam registradas, então o cliente pode tentar de novo.
 *
 * A chave é escopada ao cliente (hash da X-API-Key, ou IP quando não há chave).
 */
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Idempotency-Key";
    public static final String HEADER_REPLAY = "Idempotent-Replayed";

    private static final Pattern FORMATO = Pattern.compile("^[A-Za-z0-9_:.\\-]{1,255}$");

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final ErroHttpWriter erroHttpWriter;

    public IdempotencyFilter(Duration ttl, ErroHttpWriter erroHttpWriter) {
        this.ttl = ttl;
        this.erroHttpWriter = erroHttpWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod()) || request.getHeader(HEADER) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String chaveIdempotente = request.getHeader(HEADER);
        if (!FORMATO.matcher(chaveIdempotente).matches()) {
            erroHttpWriter.escrever(request, response, HttpStatus.BAD_REQUEST, "Cabeçalho Inválido",
                    "X-Idempotency-Key deve ter de 1 a 255 caracteres: letras, números, '-', '_', ':' ou '.'.");
            return;
        }

        CorpoCacheadoRequest requisicao = new CorpoCacheadoRequest(request);
        String impressao = impressaoDa(requisicao);

        String identidade = Optional.ofNullable(request.getHeader(ApiKeyAuthFilter.HEADER))
                .filter(valor -> !valor.isBlank())
                .orElse(request.getRemoteAddr());
        String chaveRegistro = HashUtil.sha256(identidade) + ":" + chaveIdempotente;

        Instant agora = Instant.now();
        registros.values().removeIf(registro -> registro.expirou(agora));

        Registro novo = new Registro(impressao, agora.plus(ttl));
        Registro existente = registros.putIfAbsent(chaveRegistro, novo);

        if (existente == null) {
            processar(chaveRegistro, novo, requisicao, response, filterChain);
            return;
        }
        if (!existente.impressao.equals(impressao)) {
            erroHttpWriter.escrever(request, response, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Requisição Idempotente Inválida",
                    "A X-Idempotency-Key já foi usada com outro método, rota ou corpo. Use uma nova chave.");
            return;
        }
        if (!existente.concluida) {
            erroHttpWriter.escrever(request, response, HttpStatus.CONFLICT, "Requisição Em Processamento",
                    "Uma requisição com esta X-Idempotency-Key ainda está em processamento. Aguarde e tente novamente.");
            return;
        }
        reproduzir(existente, response);
    }

    private void processar(String chaveRegistro, Registro registro, HttpServletRequest request,
                           HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingResponseWrapper respostaCacheada = new ContentCachingResponseWrapper(response);
        boolean armazenada = false;
        try {
            filterChain.doFilter(request, respostaCacheada);
            int status = respostaCacheada.getStatus();
            if (status >= 200 && status < 300) {
                registro.concluir(status, respostaCacheada.getContentType(),
                        respostaCacheada.getHeader("Location"), respostaCacheada.getContentAsByteArray());
                armazenada = true;
            }
        } finally {
            if (!armazenada) {
                registros.remove(chaveRegistro);
            }
        }
        respostaCacheada.copyBodyToResponse();
    }

    private void reproduzir(Registro registro, HttpServletResponse response) throws IOException {
        response.setStatus(registro.status);
        if (registro.contentType != null) {
            response.setContentType(registro.contentType);
        }
        if (registro.location != null) {
            response.setHeader("Location", registro.location);
        }
        response.setHeader(HEADER_REPLAY, "true");
        response.getOutputStream().write(registro.corpo);
    }

    /** Impressão digital: método, URI, query string e corpo. */
    private static String impressaoDa(CorpoCacheadoRequest requisicao) {
        String query = requisicao.getQueryString() == null ? "" : requisicao.getQueryString();
        String rota = requisicao.getMethod() + " " + requisicao.getRequestURI() + "?" + query;
        return HashUtil.sha256(rota) + ":" + HashUtil.sha256(requisicao.getCorpo());
    }

    /** Lê o corpo uma única vez, para calcular a impressão digital e depois repassá-lo ao controller. */
    static final class CorpoCacheadoRequest extends HttpServletRequestWrapper {

        private final byte[] corpo;

        CorpoCacheadoRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.corpo = request.getInputStream().readAllBytes();
        }

        byte[] getCorpo() {
            return corpo;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream fluxo = new ByteArrayInputStream(corpo);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return fluxo.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException("Leitura assíncrona não suportada");
                }

                @Override
                public int read() {
                    return fluxo.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    private static final class Registro {

        private final String impressao;
        private final Instant expiraEm;
        private volatile boolean concluida;
        private volatile int status;
        private volatile String contentType;
        private volatile String location;
        private volatile byte[] corpo;

        Registro(String impressao, Instant expiraEm) {
            this.impressao = impressao;
            this.expiraEm = expiraEm;
        }

        void concluir(int status, String contentType, String location, byte[] corpo) {
            this.status = status;
            this.contentType = contentType;
            this.location = location;
            this.corpo = corpo;
            this.concluida = true; // por último: quem enxergar "concluida" vê os demais campos
        }

        boolean expirou(Instant agora) {
            return agora.isAfter(expiraEm);
        }
    }
}
