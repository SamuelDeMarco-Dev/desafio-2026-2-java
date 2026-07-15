package br.com.samuel.documentos_academicos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired UsuarioRepository usuarioRepository;

    @Test
    void usuarioInativoNaoEEncontradoPelaConsultaDeAtivos() {
        usuarioRepository.save(usuario("inativo", false, Set.of(Perfil.CONSULTA)));

        assertTrue(usuarioRepository.findByLoginAndAtivoTrue("inativo").isEmpty(),
                "usuário inativo não pode ser retornado na consulta de ativos");
        assertTrue(usuarioRepository.findByLogin("inativo").isPresent(),
                "o usuário continua existindo, apenas não está ativo");
    }

    @Test
    void usuarioAtivoEEncontradoComPerfisPersistidos() {
        usuarioRepository.save(usuario("samuel", true, Set.of(Perfil.ADMIN, Perfil.OPERADOR)));

        var encontrado = usuarioRepository.findByLoginAndAtivoTrue("samuel");

        assertTrue(encontrado.isPresent());
        assertEquals(2, encontrado.get().getPerfis().size());
        assertTrue(encontrado.get().getPerfis().contains(Perfil.ADMIN));
        assertTrue(encontrado.get().getPerfis().contains(Perfil.OPERADOR));
    }

    private Usuario usuario(String login, boolean ativo, Set<Perfil> perfis) {
        Usuario u = new Usuario();
        u.setNome("Usuário " + login);
        u.setLogin(login);
        u.setSenha("$2a$10$hashDeTesteQualquer");
        u.setAtivo(ativo);
        u.setPerfis(new HashSet<>(perfis));
        return u;
    }
}