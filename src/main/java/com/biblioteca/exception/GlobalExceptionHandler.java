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

    // 1. Trata violações de integridade do banco (chaves únicas, FKs e restrições @OneToOne) -> 409 CONFLICT
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("erro", "Recurso em uso ou registro duplicado.");
        resposta.put("detalhes", "Não foi possível concluir a operação pois o registro possui dependências vinculadas ou o dado informado já existe no sistema.");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }

    // 2. Trata erros de validação dos campos do DTO/Entidade (@Valid, @NotBlank, etc.) -> 400 BAD REQUEST
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

    // 3. Trata exceções de regras de negócio (ex: tentar emprestar livro indisponível) -> 400 BAD REQUEST
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBusinessRules(RuntimeException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // 4. Trata erros de leitura no JSON (sintaxe ou enum/datas incorretas) -> 400 BAD REQUEST
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", "Corpo da requisição malformatado ou valor de campo numérico/Enum inválido.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // 5. Trata erros de tipo nos parâmetros da URL (ex: passar texto num ID) -> 400 BAD REQUEST
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", String.format("O parâmetro '%s' deve ser um valor numérico válido.", ex.getName()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // 6. Trata exceções de entidades não encontradas -> 404 NOT FOUND
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

    // 7. Captura genérica para erros não previstos no sistema -> 500 INTERNAL SERVER ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", "Ocorreu um erro interno inesperado no servidor.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}