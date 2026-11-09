
package com.biblioteca.repository;

import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.StatusEmprestimo;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    // Buscar empréstimos por status com paginação.
    Page<Emprestimo> findByStatus(
            StatusEmprestimo status,
            Pageable pageable
    );

    // Buscar empréstimos de um livro por determinados status.
    List<Emprestimo> findByLivroIdAndStatusIn(
            Long livroId,
            List<StatusEmprestimo> status
    );

    // Buscar empréstimos de um usuário.
    List<Emprestimo> findByUsuarioId(Long usuarioId);

    // Verificar se o usuário já possui empréstimo ativo ou atrasado
    // do mesmo livro.
    boolean existsByUsuarioIdAndLivroIdAndStatusIn(
            Long usuarioId,
            Long livroId,
            List<StatusEmprestimo> status
    );

    // Buscar empréstimo com bloqueio para evitar alterações simultâneas.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Emprestimo e WHERE e.id = :id")
    Optional<Emprestimo> findByIdComBloqueio(
            @Param("id") Long id
    );
}
