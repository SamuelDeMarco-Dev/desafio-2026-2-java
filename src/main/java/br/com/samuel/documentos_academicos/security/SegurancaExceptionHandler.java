package br.com.samuel.documentos_academicos.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.samuel.documentos_academicos.dto.response.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Traduz as falhas de segurança para o mesmo formato do ErroResponse:
 * 401 quando não há autenticação e 403 quando o perfil não tem permissão.
 */
public class SegurancaExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SegurancaExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Sem token (ou não autenticado) -> 401. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escrever(request, response, HttpStatus.UNAUTHORIZED,
                "Não autenticado", "Autenticação é obrigatória para acessar este recurso");
    }

    /** Autenticado, mas sem o perfil exigido -> 403. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escrever(request, response, HttpStatus.FORBIDDEN,
                "Acesso negado", "Seu perfil não tem permissão para esta operação");
    }

    private void escrever(HttpServletRequest request, HttpServletResponse response,
                          HttpStatus status, String erro, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErroResponse corpo = new ErroResponse(
                LocalDateTime.now(), status.value(), erro, mensagem, request.getRequestURI(), List.of());
        objectMapper.writeValue(response.getWriter(), corpo);
    }
}