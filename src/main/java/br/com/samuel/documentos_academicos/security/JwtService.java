package br.com.samuel.documentos_academicos.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Final de propósito: o construtor lança exceção quando a chave é curta demais
 * (fail-fast do JWT_SECRET), e um construtor que lança deixa o objeto
 * parcialmente construído ao alcance de um finalizer attack via subclasse.
 * Sendo final, a classe não pode ser estendida e o ataque não existe.
 */
@Component
public final class JwtService {

    private final SecretKey chave;
    private final long expiracaoSegundos;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiracao-segundos}") long expiracaoSegundos) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoSegundos = expiracaoSegundos;
    }

    public long getExpiracaoSegundos() {
        return expiracaoSegundos;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getLogin())
                .claim("userId", usuario.getId())
                .claim("responsavel", usuario.getCodigoResponsavel())
                .claim("roles", usuario.getPerfis().stream().map(Enum::name).toList())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoSegundos)))
                .signWith(chave)
                .compact();
    }

    /** Lança JwtException quando o token está expirado, adulterado ou malformado. */
    public Jws<Claims> validar(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token);
    }
}