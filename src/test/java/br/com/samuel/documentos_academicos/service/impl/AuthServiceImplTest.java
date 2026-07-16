package br.com.samuel.documentos_academicos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.samuel.documentos_academicos.dto.request.LoginRequest;
import br.com.samuel.documentos_academicos.dto.response.TokenResponse;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;
import br.com.samuel.documentos_academicos.exception.CredenciaisInvalidasException;
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

    @BeforeEach
    void setup() {
        service = new AuthServiceImpl(usuarioRepository, encoder, jwtService);
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
}