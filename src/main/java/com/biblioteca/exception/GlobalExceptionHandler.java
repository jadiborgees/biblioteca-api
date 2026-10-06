package com.biblioteca.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Trata violações de integridade do banco (chaves únicas, FKs e restrições @OneToOne)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", "Operação inválida: o registro já existe, o endereço informado já está vinculado a outro utilizador ou a entidade possui dependências associadas.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // 2. Trata erros de validação dos campos do DTO/Entidade (@Valid, @NotBlank, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> resposta = new HashMap<>();
        Map<String, String> errosCampos = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errosCampos.put(error.getField(), error.getDefaultMessage())
        );

        resposta.put("erro", "Dados de entrada inválidos.");
        resposta.put("detalhes", errosCampos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    // 3. Trata exceções de regras de negócio (ex: tentar emprestar livro indisponível)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBusinessRules(RuntimeException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // 4. Trata erros de leitura no JSON (ex: sintaxe incorreta ou enum inválido no corpo da requisição)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", "Corpo da requisição malformatado ou valor de campo numérico/Enum inválido.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // 5. Trata erros de tipo nos parâmetros da URL (ex: passar letras em um parâmetro ID)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", String.format("O parâmetro '%s' deve ser um valor numérico válido.", ex.getName()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // 6. Trata exceções de entidades não encontradas
    @ExceptionHandler({
            AutorNotFoundException.class,
            EmprestimoNotFoundException.class,
            EnderecoNotFoundException.class,
            LivroNotFoundException.class,
            UsuarioNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFoundExceptions(RuntimeException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }
}