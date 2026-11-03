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
            EmprestimoRepository emprestimoRepository,
            ApiKeyRepository apiKeyRepository) {

        return args -> {
            log.info("Carregando dados de teste no H2...");

            // Criar Chave de API de teste inicial
            ApiKey apiKeyDemo = new ApiKey("bib-chave-demo-12345", "joao@email.com");
            apiKeyRepository.save(apiKeyDemo);
            log.info("Chave de API inicial gerada: bib-chave-demo-12345");

            // Criar Autor
            Autor autor1 = new Autor();
            autor1.setNome("Machado de Assis");
            autor1.setNacionalidade("Brasileira");
            autorRepository.save(autor1);

            // Criar Endereço
            Endereco endereco1 = new Endereco();
            endereco1.setLogradouro("Rua das Flores");
            endereco1.setNumero("123");
            endereco1.setBairro("Centro");
            endereco1.setCidade("São Paulo");
            endereco1.setEstado("SP");
            endereco1.setCep("01001-000");

            // Criar Usuário
            Usuario usuario1 = new Usuario();
            usuario1.setNome("João Silva");
            usuario1.setEmail("joao@email.com");
            usuario1.setTelefone("11999998888");
            usuario1.setDataNascimento(LocalDate.of(1990, 5, 15));
            usuario1.setEndereco(endereco1);

            Usuario usuarioSalvo = usuarioRepository.save(usuario1);

            // Criar Livro
            Livro livro1 = new Livro();
            livro1.setTitulo("Dom Casmurro");
            livro1.setIsbn("9788535902778");
            livro1.setAnoPublicacao(1899);
            livro1.setAutores(Set.of(autor1));
            Livro livroSalvo = livroRepository.save(livro1);

            // Criar Empréstimo
            Emprestimo emprestimo1 = new Emprestimo();
            emprestimo1.setDataEmprestimo(LocalDate.now());
            emprestimo1.setDataDevolucaoPrevista(LocalDate.now().plusDays(14));
            emprestimo1.setStatus(StatusEmprestimo.ATIVO);
            emprestimo1.setUsuario(usuarioSalvo);
            emprestimo1.setLivro(livroSalvo);

            emprestimoRepository.save(emprestimo1);

            log.info("Dados de teste carregados com sucesso!");
        };
    }
}