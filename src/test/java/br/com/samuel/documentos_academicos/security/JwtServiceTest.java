package br.com.samuel.documentos_academicos.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "chave-de-teste-apenas-para-os-testes-automatizados";

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Samuel");
        u.setLogin("samuel");
        u.setCodigoResponsavel(1001);
        u.setAtivo(true);
        u.setPerfis(Set.of(Perfil.ADMIN));
        return u;
    }

    @Test
    void gerarTokenIncluiClaimsEsperados() {
        JwtService service = new JwtService(SECRET, 3600);

        Claims claims = service.validar(service.gerarToken(usuario())).getPayload();

        assertEquals("samuel", claims.getSubject());
        assertEquals(1, ((Number) claims.get("userId")).intValue());
        assertEquals(1001, ((Number) claims.get("responsavel")).intValue());
        assertTrue(((List<?>) claims.get("roles")).contains("ADMIN"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void tokenExpiradoERejeitado() {
        JwtService vencido = new JwtService(SECRET, -10); // já nasce expirado
        String token = vencido.gerarToken(usuario());

        assertThrows(ExpiredJwtException.class, () -> vencido.validar(token));
    }

    @Test
    void tokenAdulteradoERejeitado() {
        JwtService service = new JwtService(SECRET, 3600);
        String token = service.gerarToken(usuario());
        String adulterado = token.substring(0, token.length() - 2) + "xx";

        assertThrows(JwtException.class, () -> service.validar(adulterado));
    }

    @Test
    void tokenAssinadoComOutraChaveERejeitado() {
        String token = new JwtService("outra-chave-bem-diferente-com-mais-de-32-chars", 3600)
                .gerarToken(usuario());
        JwtService service = new JwtService(SECRET, 3600);

        assertThrows(JwtException.class, () -> service.validar(token));
    }
}