
package com.biblioteca.config;

import com.biblioteca.exception.ErroHttpWriter;
import com.biblioteca.filter.ApiKeyAuthFilter;
import com.biblioteca.filter.IdempotencyFilter;
import com.biblioteca.filter.RateLimitFilter;
import com.biblioteca.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.time.Duration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
            @Value("${biblioteca.rate-limit.capacidade:30}") long capacidade,
            @Value("${biblioteca.rate-limit.periodo-segundos:60}") long periodoSegundos,
            ErroHttpWriter erroHttpWriter) {

        FilterRegistrationBean<RateLimitFilter> registro =
                new FilterRegistrationBean<>(
                        new RateLimitFilter(
                                capacidade,
                                Duration.ofSeconds(periodoSegundos),
                                erroHttpWriter
                        )
                );

        registro.addUrlPatterns("/*");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);

        return registro;
    }

    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilter(
            ApiKeyService apiKeyService,
            ErroHttpWriter erroHttpWriter) {

        FilterRegistrationBean<ApiKeyAuthFilter> registro =
                new FilterRegistrationBean<>(
                        new ApiKeyAuthFilter(
                                apiKeyService,
                                erroHttpWriter
                        )
                );

        registro.addUrlPatterns("/*");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);

        return registro;
    }

    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilter(
            @Value("${biblioteca.idempotency.ttl-horas:24}") long ttlHoras,
            ErroHttpWriter erroHttpWriter) {

        FilterRegistrationBean<IdempotencyFilter> registro =
                new FilterRegistrationBean<>(
                        new IdempotencyFilter(
                                Duration.ofHours(ttlHoras),
                                erroHttpWriter
                        )
                );

        registro.addUrlPatterns("/*");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);

        return registro;
    }
}
