
package com.biblioteca.repository;

import com.biblioteca.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    Page<Livro> findByTituloContainingIgnoreCase(
            String titulo,
            Pageable pageable
    );

    boolean existsByIsbn(String isbn);

    Optional<Livro> findByIsbn(String isbn);
}
