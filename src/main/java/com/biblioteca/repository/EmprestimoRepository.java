package com.biblioteca.repository;

import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.StatusEmprestimo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    Page<Emprestimo> findByStatus(StatusEmprestimo status, Pageable pageable);
    List<Emprestimo> findByLivroIdAndStatusIn(Long livroId, List<StatusEmprestimo> status);
    Page<Emprestimo> findByUsuarioId(Long usuarioId, Pageable pageable);
}