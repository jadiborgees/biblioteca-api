package com.biblioteca;

import com.biblioteca.model.*;
import com.biblioteca.repository.*;
import com.biblioteca.service.ApiKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(
            AutorRepository autorRepository,
            UsuarioRepository usuarioRepository,
            LivroRepository livroRepository,
            EmprestimoRepository emprestimoRepository,
            ApiKeyService apiKeyService) {

        return args -> {
            log.info("Carregando dados de teste no H2...");

            // Chaves de desenvolvimento, uma por nível de acesso (NÃO usar em produção)
            apiKeyService.registrarChaveConhecida("bib-chave-demo-12345", "joao@email.com", NivelAcesso.ADMIN);
            apiKeyService.registrarChaveConhecida("bib-operador-demo-12345", "operador@biblioteca.com", NivelAcesso.OPERADOR);
            apiKeyService.registrarChaveConhecida("bib-leitor-demo-12345", "leitor@biblioteca.com", NivelAcesso.LEITOR);
            log.info("Chaves de desenvolvimento: ADMIN=bib-chave-demo-12345, OPERADOR=bib-operador-demo-12345, LEITOR=bib-leitor-demo-12345");

            // ---------- Autores ----------
            Autor machado = autor(autorRepository, "Machado de Assis", "Brasileira");
            Autor aluisio = autor(autorRepository, "Aluísio Azevedo", "Brasileira");
            Autor clarice = autor(autorRepository, "Clarice Lispector", "Brasileira");
            Autor graciliano = autor(autorRepository, "Graciliano Ramos", "Brasileira");
            Autor jorge = autor(autorRepository, "Jorge Amado", "Brasileira");
            Autor alencar = autor(autorRepository, "José de Alencar", "Brasileira");
            Autor lima = autor(autorRepository, "Lima Barreto", "Brasileira");
            Autor rachel = autor(autorRepository, "Rachel de Queiroz", "Brasileira");
            Autor drummond = autor(autorRepository, "Carlos Drummond de Andrade", "Brasileira");
            Autor cecilia = autor(autorRepository, "Cecília Meireles", "Brasileira");

            // ---------- Livros (ISBNs de exemplo, apenas para demonstração) ----------
            Livro domCasmurro = livro(livroRepository, "Dom Casmurro", "9788535902778", 1899, machado);
            Livro bras = livro(livroRepository, "Memórias Póstumas de Brás Cubas", "9788535914047", 1881, machado);
            Livro quincas = livro(livroRepository, "Quincas Borba", "9788508041534", 1891, machado);
            Livro cortico = livro(livroRepository, "O Cortiço", "9788508040605", 1890, aluisio);
            Livro hora = livro(livroRepository, "A Hora da Estrela", "9788532508117", 1977, clarice);
            Livro lacos = livro(livroRepository, "Laços de Família", "9788532507129", 1960, clarice);
            Livro vidas = livro(livroRepository, "Vidas Secas", "9788501111999", 1938, graciliano);
            Livro bernardo = livro(livroRepository, "São Bernardo", "9788501111234", 1934, graciliano);
            Livro capitaes = livro(livroRepository, "Capitães da Areia", "9788535914849", 1937, jorge);
            Livro gabriela = livro(livroRepository, "Gabriela, Cravo e Canela", "9788535914856", 1958, jorge);
            Livro iracema = livro(livroRepository, "Iracema", "9788508132980", 1865, alencar);
            Livro guarani = livro(livroRepository, "O Guarani", "9788508132997", 1857, alencar);
            Livro policarpo = livro(livroRepository, "Triste Fim de Policarpo Quaresma", "9788508133000", 1915, lima);
            Livro quinze = livro(livroRepository, "O Quinze", "9788503010320", 1930, rachel);
            Livro sentimento = livro(livroRepository, "Sentimento do Mundo", "9788535925470", 1940, drummond);
            Livro romanceiro = livro(livroRepository, "Romanceiro da Inconfidência", "9788526009363", 1953, cecilia);

            // ---------- Usuários ----------
            Usuario joao = usuario(usuarioRepository, "João Silva", "joao@email.com", "(11)99999-8888",
                    LocalDate.of(1990, 5, 15),
                    endereco("Rua das Flores", "123", "Centro", "São Paulo", "SP", "01001-000"));
            Usuario maria = usuario(usuarioRepository, "Maria Santos", "maria@email.com", "(11)97777-6666",
                    LocalDate.of(1998, 11, 20),
                    endereco("Av. Paulista", "1000", "Bela Vista", "São Paulo", "SP", "01310-100"));
            Usuario pedro = usuario(usuarioRepository, "Pedro Oliveira", "pedro@email.com", "(11)95555-4444",
                    LocalDate.of(1985, 3, 8),
                    endereco("Rua Augusta", "850", "Consolação", "São Paulo", "SP", "01304-001"));
            Usuario ana = usuario(usuarioRepository, "Ana Costa", "ana@email.com", "(11)94444-3333",
                    LocalDate.of(2001, 7, 25),
                    endereco("Rua dos Pinheiros", "420", "Pinheiros", "São Paulo", "SP", "05422-001"));
            Usuario carlos = usuario(usuarioRepository, "Carlos Souza", "carlos@email.com", "(11)93333-2222",
                    LocalDate.of(1978, 12, 2),
                    endereco("Av. Eng. Eusébio Stevaux", "300", "Santo Amaro", "São Paulo", "SP", "04696-000"));
            Usuario juliana = usuario(usuarioRepository, "Juliana Lima", "juliana@email.com", "(11)92222-1111",
                    LocalDate.of(1993, 9, 18),
                    endereco("Rua XV de Novembro", "55", "Centro", "Taboão da Serra", "SP", "06763-000"));

            // ---------- Empréstimos (um livro não pode ter dois empréstimos em aberto) ----------
            LocalDate hoje = LocalDate.now();

            // Em andamento
            emprestimo(emprestimoRepository, joao, domCasmurro, hoje, hoje.plusDays(14), null, StatusEmprestimo.ATIVO);
            emprestimo(emprestimoRepository, maria, vidas, hoje.minusDays(5), hoje.plusDays(9), null, StatusEmprestimo.ATIVO);
            emprestimo(emprestimoRepository, juliana, quinze, hoje.minusDays(2), hoje.plusDays(12), null, StatusEmprestimo.ATIVO);

            // Atrasados
            emprestimo(emprestimoRepository, pedro, hora, hoje.minusDays(30), hoje.minusDays(16), null, StatusEmprestimo.ATRASADO);
            emprestimo(emprestimoRepository, ana, capitaes, hoje.minusDays(20), hoje.minusDays(6), null, StatusEmprestimo.ATRASADO);

            // Já devolvidos
            emprestimo(emprestimoRepository, carlos, iracema, hoje.minusDays(40), hoje.minusDays(26), hoje.minusDays(28), StatusEmprestimo.DEVOLVIDO);
            emprestimo(emprestimoRepository, maria, cortico, hoje.minusDays(25), hoje.minusDays(11), hoje.minusDays(12), StatusEmprestimo.DEVOLVIDO);
            emprestimo(emprestimoRepository, joao, sentimento, hoje.minusDays(60), hoje.minusDays(46), hoje.minusDays(47), StatusEmprestimo.DEVOLVIDO);

            log.info("Dados de teste carregados com sucesso!");
        };
    }

    private static Autor autor(AutorRepository repo, String nome, String nacionalidade) {
        Autor autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);
        return repo.save(autor);
    }

    private static Livro livro(LivroRepository repo, String titulo, String isbn, int ano, Autor... autores) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setIsbn(isbn);
        livro.setAnoPublicacao(ano);
        livro.setAutores(new HashSet<>(Arrays.asList(autores)));
        return repo.save(livro);
    }

    private static Endereco endereco(String logradouro, String numero, String bairro,
                                     String cidade, String estado, String cep) {
        Endereco endereco = new Endereco();
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setEstado(estado);
        endereco.setCep(cep);
        return endereco;
    }

    private static Usuario usuario(UsuarioRepository repo, String nome, String email, String telefone,
                                   LocalDate nascimento, Endereco endereco) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setDataNascimento(nascimento);
        usuario.setEndereco(endereco);
        return repo.save(usuario);
    }

    private static void emprestimo(EmprestimoRepository repo, Usuario usuario, Livro livro,
                                   LocalDate dataEmprestimo, LocalDate dataPrevista,
                                   LocalDate dataDevolucao, StatusEmprestimo status) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(dataEmprestimo);
        emprestimo.setDataDevolucaoPrevista(dataPrevista);
        emprestimo.setDataDevolucao(dataDevolucao);
        emprestimo.setStatus(status);
        repo.save(emprestimo);
    }
}