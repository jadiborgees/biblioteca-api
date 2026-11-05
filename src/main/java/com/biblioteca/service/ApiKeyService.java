
package com.biblioteca.service;

import com.biblioteca.exception.ApiKeyNotFoundException;
import com.biblioteca.model.ApiKey;
import com.biblioteca.model.NivelAcesso;
import com.biblioteca.repository.ApiKeyRepository;
import com.biblioteca.util.HashUtil;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class ApiKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIXO = "bib-";

    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    /**
     * Resultado da geração:
     * entidade persistida e chave em texto puro (exibida uma única vez).
     */
    public record ChaveGerada(ApiKey entidade, String chavePlana) {}

    public ChaveGerada gerar(String usuario, NivelAcesso nivel) {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);

        String chave = PREFIXO + HexFormat.of().formatHex(bytes);

        ApiKey salva = repository.save(
                new ApiKey(
                        HashUtil.sha256(chave),
                        mascarar(chave),
                        usuario,
                        nivel
                )
        );

        return new ChaveGerada(salva, chave);
    }

    /**
     * Usado para semear chaves conhecidas em ambiente de desenvolvimento.
     * Evita cadastrar novamente uma chave que já existe no banco.
     */
    public ApiKey registrarChaveConhecida(
            String chave,
            String usuario,
            NivelAcesso nivel
    ) {
        String hash = HashUtil.sha256(chave);

        return repository.findByChaveHash(hash)
                .orElseGet(() -> repository.save(
                        new ApiKey(hash, mascarar(chave), usuario, nivel)
                ));
    }

    public Optional<ApiKey> validar(String chavePlana) {
        return repository.findByChaveHashAndAtivaTrue(
                HashUtil.sha256(chavePlana)
        );
    }

    public List<ApiKey> listar() {
        return repository.findAll();
    }

    public ApiKey alterarStatus(Long id, boolean ativa) {
        ApiKey apiKey = buscar(id);
        apiKey.setAtiva(ativa);

        return repository.save(apiKey);
    }

    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    private ApiKey buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiKeyNotFoundException(id));
    }

    public static String mascarar(String chave) {
        if (chave.length() <= 12) {
            return "****";
        }

        return chave.substring(0, 8)
                + "..."
                + chave.substring(chave.length() - 4);
    }
}
