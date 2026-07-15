package br.com.samuel.documentos_academicos.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.samuel.documentos_academicos.dto.request.UsuarioRequest;
import br.com.samuel.documentos_academicos.dto.response.UsuarioResponse;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;
import br.com.samuel.documentos_academicos.exception.RecursoDuplicadoException;
import br.com.samuel.documentos_academicos.mapper.UsuarioMapper;
import br.com.samuel.documentos_academicos.repository.UsuarioRepository;
import br.com.samuel.documentos_academicos.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final String SENHA_PURA = "senhaSuperSecreta";

    @Mock UsuarioRepository usuarioRepository;

    UsuarioService service;
    final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        service = new UsuarioServiceImpl(usuarioRepository, new UsuarioMapper(), encoder);
    }

    private UsuarioRequest request() {
        return new UsuarioRequest("Samuel", "samuel", SENHA_PURA, 1001, Set.of(Perfil.ADMIN));
    }

    private void stubSalvar() {
        when(usuarioRepository.save(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
    }

    private Usuario capturarSalvo() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void senhaEArmazenadaComBCryptENuncaEmTextoPuro() {
        stubSalvar();

        service.criar(request());

        String hash = capturarSalvo().getSenha();
        assertNotEquals(SENHA_PURA, hash, "a senha não pode ser persistida em texto puro");
        assertTrue(hash.startsWith("$2"), "hash deve estar no formato BCrypt");
        assertTrue(encoder.matches(SENHA_PURA, hash), "o hash deve conferir com a senha original");
    }

    @Test
    void perfisSaoAssociadosEUsuarioNasceAtivo() {
        stubSalvar();

        UsuarioResponse response = service.criar(request());

        Usuario salvo = capturarSalvo();
        assertTrue(salvo.getPerfis().contains(Perfil.ADMIN));
        assertTrue(salvo.isAtivo());
        assertTrue(response.perfis().contains(Perfil.ADMIN));
    }

    @Test
    void loginDuplicadoRetornaConflito() {
        when(usuarioRepository.existsByLogin("samuel")).thenReturn(true);
        assertThrows(RecursoDuplicadoException.class, () -> service.criar(request()));
    }

    @Test
    void codigoResponsavelDuplicadoRetornaConflito() {
        when(usuarioRepository.existsByCodigoResponsavel(1001)).thenReturn(true);
        assertThrows(RecursoDuplicadoException.class, () -> service.criar(request()));
    }
}