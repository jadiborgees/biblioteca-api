package com.biblioteca.repository;

import com.biblioteca.model.Endereco;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    Page<Endereco> findByCidadeContainingIgnoreCase(
            String cidade,
            Pageable pageable
    );
}