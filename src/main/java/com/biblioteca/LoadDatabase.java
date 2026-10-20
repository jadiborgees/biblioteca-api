package com.biblioteca;

import com.biblioteca.model.*;
import com.biblioteca.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Set;

@Configuration
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(
            AutorRepository autorRepository,
            EnderecoRepository enderecoRepository,
            UsuarioRepository usuarioRepository,
            LivroRepository livroRepository,
            EmprestimoRepository emprestimoRepository) {

        return args -> {
            log.info("Carregando dados de teste no H2...");

            // 1. Autores
            Autor autor1 = new Autor();
            autor1.setNome("Machado de Assis");
            autor1.setNacionalidade("Brasileira");
            autor1 = autorRepository.save(autor1);

            Autor autor2 = new Autor();
            autor2.setNome("Aluísio Azevedo");
            autor2.setNacionalidade("Brasileira");
            autor2 = autorRepository.save(autor2);

            // 2. Endereços
            Endereco end1 = new Endereco();
            end1.setLogradouro("Rua das Flores");
            end1.setNumero("123");
            end1.setBairro("Centro");
            end1.setCidade("São Paulo");
            end1.setEstado("SP");
            end1.setCep("01001-000");
            end1 = enderecoRepository.save(end1);

            Endereco end2 = new Endereco();
            end2.setLogradouro("Avenida Paulista");
            end2.setNumero("1000");
            end2.setBairro("Bela Vista");
            end2.setCidade("São Paulo");
            end2.setEstado("SP");
            end2.setCep("01310-100");
            end2 = enderecoRepository.save(end2);

            // 3. Usuários
            Usuario user1 = new Usuario();
            user1.setNome("João Silva");
            user1.setEmail("joao@email.com");
            user1.setTelefone("11999999999");
            user1.setDataNascimento(LocalDate.of(1995, 5, 12));
            user1.setEndereco(end1);
            user1 = usuarioRepository.save(user1);

            Usuario user2 = new Usuario();
            user2.setNome("Maria Oliveira");
            user2.setEmail("maria@email.com");
            user2.setTelefone("11888888888");
            user2.setDataNascimento(LocalDate.of(1998, 8, 20));
            user2.setEndereco(end2);
            user2 = usuarioRepository.save(user2);

            // 4. Livros
            Livro livro1 = new Livro();
            livro1.setTitulo("Dom Casmurro");
            livro1.setIsbn("9788535902778");
            livro1.setAnoPublicacao(1899);
            livro1.setAutores(Set.of(autor1));
            livro1 = livroRepository.save(livro1);

            Livro livro2 = new Livro();
            livro2.setTitulo("O Cortiço");
            livro2.setIsbn("9788508040408");
            livro2.setAnoPublicacao(1890);
            livro2.setAutores(Set.of(autor2));
            livro2 = livroRepository.save(livro2);

            // 5. Empréstimos
            Emprestimo emp1 = new Emprestimo();
            emp1.setUsuario(user1);
            emp1.setLivro(livro1);
            emp1.setDataEmprestimo(LocalDate.now());
            emp1.setDataDevolucao(LocalDate.now().plusDays(14));
            emp1.setStatus(StatusEmprestimo.ATIVO);
            emprestimoRepository.save(emp1);

            log.info("Dados de teste carregados com sucesso!");
        };
    }
}