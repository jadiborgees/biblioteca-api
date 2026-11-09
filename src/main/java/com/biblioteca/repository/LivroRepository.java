
package com.biblioteca.repository;

import com.biblioteca.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select l from Livro l where l.id = :id")
    Optional<Livro> findByIdComBloqueio(@org.springframework.data.repository.query.Param("id") Long id);

    Page<Livro> findByTituloContainingIgnoreCase(
            String titulo,
            Pageable pageable
    );

    boolean existsByIsbn(String isbn);

    Optional<Livro> findByIsbn(String isbn);
}
