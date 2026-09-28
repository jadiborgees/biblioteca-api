package com.biblioteca;

import com.biblioteca.model.Livro;
import com.biblioteca.repository.LivroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(LivroRepository repository) {

        return args -> {
            repository.save(new Livro(
                    "Dom Casmurro",
                    "9788535910663",
                    1899
            ));

            repository.save(new Livro(
                    "O Cortiço",
                    "9788572328111",
                    1890
            ));

            repository.save(new Livro(
                    "Memórias Póstumas de Brás Cubas",
                    "9788535910664",
                    1881
            ));
        };
    }
}