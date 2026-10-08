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
            end1.setRua("Rua das Flores");
            end1.setNumero("123");
            end1.setBairro("Centro");
            end1.setCidade("São Paulo");
            end1.setEstado("SP");
            end1.setCep("01000000");
            end1 = enderecoRepository.save(end1);

            // 3. Usuários
            Usuario user1 = new Usuario();
            user1.setNome("Maria Silva");
            user1.setEmail("maria@email.com");
            user1.setTelefone("11988887777");
            user1.setDataNascimento(LocalDate.of(1995, 5, 12));
            user1.setEndereco(end1);
            user1 = usuarioRepository.save(user1);

            // 4. Livros
            Livro livro1 = new Livro(
                    "Dom Casmurro",
                    "9788535910663",
                    1899
            );
            livro1.setAutores(Set.of(autor1));
            livro1 = livroRepository.save(livro1);

            Livro livro2 = new Livro(
                    "O Cortiço",
                    "9788572328111",
                    1890
            );
            livro2.setAutores(Set.of(autor2));
            livro2 = livroRepository.save(livro2);

            Livro livro3 = new Livro(
                    "Memórias Póstumas de Brás Cubas",
                    "9788535910664",
                    1881
            );
            livro3.setAutores(Set.of(autor1));
            livro3 = livroRepository.save(livro3);

            // 5. Empréstimo (Ajustado para LocalDate.now() para respeitar @FutureOrPresent)
            Emprestimo emp1 = new Emprestimo();
            emp1.setDataEmprestimo(LocalDate.now());
            emp1.setDataDevolucao(LocalDate.now().plusDays(7));
            emp1.setStatus(StatusEmprestimo.ATIVO);
            emp1.setLivro(livro1);
            emp1.setUsuario(user1);
            emprestimoRepository.save(emp1);

            log.info("Carga inicial concluída com sucesso!");
        };
    }
}