package br.com.samuel.documentos_academicos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.samuel.documentos_academicos.dto.request.EsqueciSenhaRequest;
import br.com.samuel.documentos_academicos.dto.request.LoginRequest;
import br.com.samuel.documentos_academicos.dto.request.RedefinirSenhaRequest;
import br.com.samuel.documentos_academicos.dto.response.RecuperacaoSenhaResponse;
import br.com.samuel.documentos_academicos.dto.response.TokenResponse;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;
import br.com.samuel.documentos_academicos.exception.CredenciaisInvalidasException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.repository.UsuarioRepository;
import br.com.samuel.documentos_academicos.security.JwtService;
import br.com.samuel.documentos_academicos.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String SECRET = "chave-de-teste-apenas-para-os-testes-automatizados";
    private static final String SENHA = "senhaCorreta123";

    @Mock UsuarioRepository usuarioRepository;

    AuthService service;
    final PasswordEncoder encoder = new BCryptPasswordEncoder();
    final JwtService jwtService = new JwtService(SECRET, 3600);
    final Clock clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new AuthServiceImpl(usuarioRepository, encoder, jwtService, clock);
    }

    private Usuario usuarioAtivo() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Samuel");
        u.setLogin("samuel");
        u.setSenha(encoder.encode(SENHA));
        u.setCodigoResponsavel(1001);
        u.setAtivo(true);
        u.setPerfis(Set.of(Perfil.ADMIN));
        return u;
    }

    @Test
    void loginValidoGeraToken() {
        when(usuarioRepository.findByLoginAndAtivoTrue("samuel")).thenReturn(Optional.of(usuarioAtivo()));

        TokenResponse response = service.autenticar(new LoginRequest("samuel", SENHA));

        assertEquals("Bearer", response.tipo());
        assertNotNull(response.token());
        assertEquals(3600, response.expiraEm());
        assertEquals("samuel", jwtService.validar(response.token()).getPayload().getSubject());
    }

    @Test
    void senhaIncorretaLancaCredenciaisInvalidas() {
        when(usuarioRepository.findByLoginAndAtivoTrue("samuel")).thenReturn(Optional.of(usuarioAtivo()));

        assertThrows(CredenciaisInvalidasException.class,
                () -> service.autenticar(new LoginRequest("samuel", "senhaErrada")));
    }

    @Test
    void usuarioInativoOuInexistenteLancaCredenciaisInvalidas() {
        // findByLoginAndAtivoTrue vazio cobre os dois casos: não existe ou está inativo
        when(usuarioRepository.findByLoginAndAtivoTrue("fantasma")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class,
                () -> service.autenticar(new LoginRequest("fantasma", SENHA)));
    }

    // ----- recuperação de senha -----

    @Test
    void gerarCodigo_paraLoginExistente_salvaHashEValidade() {
        Usuario usuario = usuarioAtivo();
        when(usuarioRepository.findByLoginAndAtivoTrue("samuel")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        RecuperacaoSenhaResponse resposta = service.gerarCodigoRecuperacao(new EsqueciSenhaRequest("samuel"));

        assertNotNull(resposta.codigoRecuperacao());
        assertEquals(6, resposta.codigoRecuperacao().length());
        assertTrue(encoder.matches(resposta.codigoRecuperacao(), usuario.getRecuperacaoTokenHash()),
                "o hash salvo deve corresponder ao código devolvido");
        assertEquals(LocalDateTime.now(clock).plusMinutes(15), usuario.getRecuperacaoExpiraEm());
    }

    @Test
    void gerarCodigo_paraLoginInexistente_respondeGenericoSemCodigo() {
        when(usuarioRepository.findByLoginAndAtivoTrue("fantasma")).thenReturn(Optional.empty());

        RecuperacaoSenhaResponse resposta = service.gerarCodigoRecuperacao(new EsqueciSenhaRequest("fantasma"));

        assertNull(resposta.codigoRecuperacao());
        assertNotNull(resposta.mensagem());
    }

    @Test
    void redefinirSenha_comCodigoValido_trocaSenhaELimpaCodigo() {
        Usuario usuario = usuarioAtivo();
        usuario.setRecuperacaoTokenHash(encoder.encode("123456"));
        usuario.setRecuperacaoExpiraEm(LocalDateTime.now(clock).plusMinutes(5));
        when(usuarioRepository.findByLoginAndAtivoTrue("samuel")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        service.redefinirSenha(new RedefinirSenhaRequest("samuel", "123456", "senhaNova123"));

        assertTrue(encoder.matches("senhaNova123", usuario.getSenha()));
        assertNull(usuario.getRecuperacaoTokenHash(), "código é de uso único");
        assertNull(usuario.getRecuperacaoExpiraEm());
    }

    @Test
    void redefinirSenha_comCodigoErrado_lancaRegraNegocio() {
        Usuario usuario = usuarioAtivo();
        usuario.setRecuperacaoTokenHash(encoder.encode("123456"));
        usuario.setRecuperacaoExpiraEm(LocalDateTime.now(clock).plusMinutes(5));
        when(usuarioRepository.findByLoginAndAtivoTrue("samuel")).thenReturn(Optional.of(usuario));

        assertThrows(RegraNegocioException.class,
                () -> service.redefinirSenha(new RedefinirSenhaRequest("samuel", "000000", "senhaNova123")));
    }

    @Test
    void redefinirSenha_comCodigoExpirado_lancaRegraNegocio() {
        Usuario usuario = usuarioAtivo();
        usuario.setRecuperacaoTokenHash(encoder.encode("123456"));
        usuario.setRecuperacaoExpiraEm(LocalDateTime.now(clock).minusMinutes(1));
        when(usuarioRepository.findByLoginAndAtivoTrue("samuel")).thenReturn(Optional.of(usuario));

        assertThrows(RegraNegocioException.class,
                () -> service.redefinirSenha(new RedefinirSenhaRequest("samuel", "123456", "senhaNova123")));
    }

    @Test
    void redefinirSenha_semCodigoPendente_lancaRegraNegocio() {
        when(usuarioRepository.findByLoginAndAtivoTrue("samuel")).thenReturn(Optional.of(usuarioAtivo()));

        assertThrows(RegraNegocioException.class,
                () -> service.redefinirSenha(new RedefinirSenhaRequest("samuel", "123456", "senhaNova123")));
    }
}