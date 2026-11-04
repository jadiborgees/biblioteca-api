package com.biblioteca.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 404 ----------
    @ExceptionHandler({
            LivroNotFoundException.class,
            AutorNotFoundException.class,
            UsuarioNotFoundException.class,
            EnderecoNotFoundException.class,
            EmprestimoNotFoundException.class,
            ApiKeyNotFoundException.class
    })
    public ResponseEntity<ErroResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso Não Encontrado", ex.getMessage(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> handleRotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Rota Não Encontrada",
                "Nenhum recurso existe no caminho " + req.getMethod() + " " + req.getRequestURI() + ".", req, null);
    }

    // ---------- 400 ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacaoCorpo(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Erro de Validação",
                "Um ou mais campos preenchidos são inválidos.", req, campos);
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ErroResponse> handleValidacaoParametros(Exception ex, HttpServletRequest req) {
        String mensagem;
        if (ex instanceof ConstraintViolationException cve) {
            mensagem = cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
        } else {
            mensagem = ((HandlerMethodValidationException) ex).getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        return resposta(HttpStatus.BAD_REQUEST, "Erro de Validação", mensagem, req, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleCorpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da Requisição Inválido",
                "O corpo da requisição está ausente ou mal formatado (JSON inválido ou valor incompatível com o campo).",
                req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro Inválido",
                "O valor '" + ex.getValue() + "' é inválido para o parâmetro '" + ex.getName() + "'.", req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> handleParametroAusente(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro Obrigatório Ausente",
                "O parâmetro obrigatório '" + ex.getParameterName() + "' não foi informado.", req, null);
    }

    @ExceptionHandler({UnsupportedApiVersionException.class, RequisicaoInvalidaException.class})
    public ResponseEntity<ErroResponse> handleRequisicaoInvalida(RuntimeException ex, HttpServletRequest req) {
        String titulo = ex instanceof UnsupportedApiVersionException
                ? "Versão de API Não Suportada" : "Requisição Inválida";
        return resposta(HttpStatus.BAD_REQUEST, titulo, ex.getMessage(), req, null);
    }

    // ---------- 405 / 409 / 415 ----------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> handleMetodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método Não Permitido",
                "O método " + ex.getMethod() + " não é suportado para esta rota.", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> handleMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de Conteúdo Não Suportado",
                "Use o cabeçalho Content-Type: application/json.", req, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Conflito de Regra de Negócio", ex.getMessage(), req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handleIntegridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Conflito de Integridade de Dados",
                "A operação não pode ser concluída: o registro tem vínculos ativos ou dados duplicados.", req, null);
    }

    // ---------- 500 ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro Interno do Servidor",
                "Ocorreu um erro inesperado no sistema. Tente novamente mais tarde.", req, null);
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String titulo, String mensagem,
                                                  HttpServletRequest req, Map<String, String> campos) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), titulo, mensagem, req.getRequestURI(), campos));
    }
}
