package br.com.samuel.documentos_academicos.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.samuel.documentos_academicos.dto.response.ErroResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Valida o Bearer token quando presente e popula o SecurityContext.
 * Não é um @Component de propósito: um bean do tipo Filter seria auto-registrado
 * pelo Boot no container servlet e rodaria duas vezes.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIXO_BEARER)) {
            String token = header.substring(PREFIXO_BEARER.length());
            try {
                Claims claims = jwtService.validar(token).getPayload();
                List<SimpleGrantedAuthority> autoridades = ((List<?>) claims.get("roles")).stream()
                        .map(String::valueOf)
                        .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil))
                        .toList();
                var autenticacao = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, autoridades);
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                responder401(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void responder401(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // o token nunca é registrado em log
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErroResponse corpo = new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Token inválido",
                "Token expirado, adulterado ou malformado",
                request.getRequestURI(),
                List.of());
        objectMapper.writeValue(response.getWriter(), corpo);
    }
}