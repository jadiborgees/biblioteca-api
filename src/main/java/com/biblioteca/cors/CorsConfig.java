package com.biblioteca.cors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS como filtro de precedência máxima: preflights são respondidos antes da autenticação,
 * e as respostas de erro (401, 403, 429) também recebem os cabeçalhos CORS.
 * Origens exatas, configuradas em biblioteca.cors.allowed-origins.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter(
            @Value("${biblioteca.cors.allowed-origins}") List<String> origensPermitidas) {

        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(origensPermitidas);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of(
                "Content-Type", "Accept", "X-API-Key", "X-Idempotency-Key", "X-API-Version"));
        configuracao.setExposedHeaders(List.of(
                "Location", "Retry-After", "X-RateLimit-Limit", "X-RateLimit-Remaining",
                "X-RateLimit-Reset", "X-API-Version", "Idempotent-Replayed"));
        configuracao.setAllowCredentials(false); // autenticação por cabeçalho, sem cookies
        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/**", configuracao);

        FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(origem));
        registro.addUrlPatterns("/*");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}
