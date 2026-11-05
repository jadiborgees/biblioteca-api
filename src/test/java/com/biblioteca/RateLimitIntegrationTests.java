package com.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Limite baixo e banco próprio para esta classe: o contexto não é compartilhado
 * com os demais testes, para não contar as requisições deles.
 */
@SpringBootTest(properties = {
        "biblioteca.rate-limit.capacidade=3",
        "biblioteca.rate-limit.periodo-segundos=60",
        "spring.datasource.url=jdbc:h2:mem:ratelimit;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class RateLimitIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void excedeOLimiteComRetryAfter() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/livros")).andExpect(status().isOk());
        }
        mvc.perform(get("/livros"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("X-RateLimit-Limit", "3"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"));
    }
}
