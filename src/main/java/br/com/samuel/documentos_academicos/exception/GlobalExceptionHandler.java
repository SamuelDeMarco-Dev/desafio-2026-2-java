package br.com.samuel.documentos_academicos.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.samuel.documentos_academicos.dto.response.ErroResponse;
import br.com.samuel.documentos_academicos.dto.response.ErroResponse.CampoErro;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErroResponse> handleDuplicado(RecursoDuplicadoException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Recurso duplicado", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio inválida", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> handleCredenciais(CredenciaisInvalidasException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new CampoErro(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Erro de validação",
                "Um ou mais campos são inválidos", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Corpo da requisição malformado ou ilegível", req, List.of());
    }

    /**
     * Sem isto o handler de Exception abaixo captura o NoResourceFoundException e
     * transforma todo path inexistente em 500 — com stack trace no log a cada
     * requisição perdida.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> handleRotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado",
                "Nenhum endpoint corresponde a " + req.getMethod() + " " + req.getRequestURI(), req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleInesperado(Exception ex, HttpServletRequest req) {
        String correlacao = UUID.randomUUID().toString();
        log.error("Erro inesperado [correlacao={}]", correlacao, ex); // stack trace apenas no log do servidor
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Código de referência: " + correlacao, req, List.of());
    }

    private ResponseEntity<ErroResponse> build(HttpStatus status, String erro, String mensagem,
            HttpServletRequest req, List<CampoErro> campos) {
        ErroResponse body = new ErroResponse(
                LocalDateTime.now(), status.value(), erro, mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResponsavelInvalidoException.class)
    public ResponseEntity<ErroResponse> handleResponsavel(ResponsavelInvalidoException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Responsável inválido", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> handleConcorrencia(ObjectOptimisticLockingFailureException ex,
                                                       HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflito de concorrência",
            "A solicitação foi alterada por outro usuário. Recarregue os dados e tente novamente.",
            req, List.of());
    }


}