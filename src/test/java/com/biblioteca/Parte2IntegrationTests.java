
package com.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da Parte 2: autenticação, CORS, versionamento,
 * idempotência e validação.
 */
@SpringBootTest
@AutoConfigureMockMvc
class Parte2IntegrationTests {

    private static final String ADMIN = "bib-chave-demo-12345";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private MockMvc mvc;

    @Test
    void termosDeUsoSaoPublicos() throws Exception {
        mvc.perform(get("/termos-de-uso"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Termos-Versao", "1.0"));
    }

    @Test
    void leituraDeLivrosEhPublica() throws Exception {
        mvc.perform(get("/livros"))
                .andExpect(status().isOk());
    }

    @Test
    void escritaSemChaveRetorna401() throws Exception {
        mvc.perform(post("/autores")
                        .contentType(JSON)
                        .content("{\"nome\":\"Autor Teste\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chaveInvalidaRetorna401() throws Exception {
        mvc.perform(get("/usuarios")
                        .header("X-API-Key", "chave-que-nao-existe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chaveDeDemonstracaoFunciona() throws Exception {
        mvc.perform(get("/usuarios")
                        .header("X-API-Key", ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    void adminPodeEscreverNosRecursos() throws Exception {
        mvc.perform(post("/autores")
                        .header("X-API-Key", ADMIN)
                        .contentType(JSON)
                        .content("{\"nome\":\"Autor Administrativo\",\"nacionalidade\":\"Brasileira\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void apenasAdminGerenciaChaves() throws Exception {
        mvc.perform(get("/api/keys")
                        .header("X-API-Key", ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    void chaveGeradaPeloAdminFunciona() throws Exception {
        MvcResult criada = mvc.perform(post("/api/keys")
                        .header("X-API-Key", ADMIN)
                        .contentType(JSON)
                        .content("{\"usuario\":\"novo@biblioteca.com\",\"nivel\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        Matcher m = Pattern.compile("\"chave\":\"(bib-[0-9a-f]+)\"")
                .matcher(criada.getResponse().getContentAsString());

        assertTrue(m.find(), "A chave deve aparecer na criação.");

        mvc.perform(get("/usuarios")
                        .header("X-API-Key", m.group(1)))
                .andExpect(status().isOk());
    }

    @Test
    void preflightDeOrigemAutorizadaRecebeCabecalhosCors() throws Exception {
        mvc.perform(options("/autores")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-api-key"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:3000"
                ));
    }

    @Test
    void preflightDeOrigemNaoAutorizadaNaoRecebeAcesso() throws Exception {
        mvc.perform(options("/autores")
                        .header("Origin", "http://site-malicioso.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void versaoNaoSuportadaRetorna400() throws Exception {
        mvc.perform(get("/livros")
                        .header("X-API-Version", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Versão de API Não Suportada"));
    }

    @Test
    void versao2EhPaginadaEIdentificada() throws Exception {
        mvc.perform(get("/livros")
                        .header("X-API-Version", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-API-Version", "2.0"))
                .andExpect(jsonPath("$.conteudo").isArray());
    }

    @Test
    void idempotenciaReproduzARespostaOriginal() throws Exception {
        String chave = "teste-" + UUID.randomUUID();
        String corpo = "{\"nome\":\"Autor Idempotente\",\"nacionalidade\":\"Brasileira\"}";

        String primeira = mvc.perform(post("/autores")
                        .header("X-API-Key", ADMIN)
                        .header("X-Idempotency-Key", chave)
                        .contentType(JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mvc.perform(post("/autores")
                        .header("X-API-Key", ADMIN)
                        .header("X-Idempotency-Key", chave)
                        .contentType(JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "true"))
                .andExpect(result -> assertEquals(
                        primeira,
                        result.getResponse().getContentAsString()
                ));
    }

    @Test
    void idempotenciaComCorpoDiferenteRetorna422() throws Exception {
        String chave = "teste-" + UUID.randomUUID();

        mvc.perform(post("/autores")
                        .header("X-API-Key", ADMIN)
                        .header("X-Idempotency-Key", chave)
                        .contentType(JSON)
                        .content("{\"nome\":\"Autor Um\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/autores")
                        .header("X-API-Key", ADMIN)
                        .header("X-Idempotency-Key", chave)
                        .contentType(JSON)
                        .content("{\"nome\":\"Autor Dois\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void validacaoRetornaErroPorCampo() throws Exception {
        mvc.perform(post("/autores")
                        .header("X-API-Key", ADMIN)
                        .contentType(JSON)
                        .content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(post("/autores")
                        .header("X-API-Key", ADMIN)
                        .contentType(JSON)
                        .content("{\"nome\": "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void idInvalidoRetorna400() throws Exception {
        mvc.perform(get("/livros/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rotaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/rota-inexistente")
                        .header("X-API-Key", ADMIN))
                .andExpect(status().isNotFound());
    }

    @Test
    void regraDeNegocioRetornaConflito() throws Exception {
        // O livro 1 já possui um empréstimo ativo na carga inicial de dados.
        String corpo = "{\"dataEmprestimo\":\"" + LocalDate.now()
                + "\",\"dataDevolucaoPrevista\":\"" + LocalDate.now().plusDays(7)
                + "\",\"usuario\":{\"id\":1},\"livro\":{\"id\":1}}";

        mvc.perform(post("/emprestimos")
                        .header("X-API-Key", ADMIN)
                        .contentType(JSON)
                        .content(corpo))
                .andExpect(status().isConflict());
    }
}
