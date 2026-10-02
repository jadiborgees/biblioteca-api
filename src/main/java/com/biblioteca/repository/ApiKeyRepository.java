package com.biblioteca.repository;

import com.biblioteca.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByChaveAndAtivaTrue(String chave);
}