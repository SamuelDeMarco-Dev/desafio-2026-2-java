package br.com.samuel.documentos_academicos.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.exception.NotAuditedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.AuditoriaRevision;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.HistoricoStatus;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;
import br.com.samuel.documentos_academicos.repository.AlunoRepository;
import br.com.samuel.documentos_academicos.repository.CursoRepository;
import br.com.samuel.documentos_academicos.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
class AuditoriaEnversTest {

    @Autowired AlunoRepository alunoRepository;
    @Autowired CursoRepository cursoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired TransactionTemplate tx;
    @Autowired EntityManager entityManager;

    @BeforeEach
    void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("administrador", null, List.of()));
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraInclusaoAlteracaoExclusaoComUsuarioEData() {
        Long id = tx.execute(s -> {
            Aluno a = new Aluno();
            a.setNome("Samuel");
            a.setAtivo(true);
            return alunoRepository.save(a).getId();
        });

        tx.executeWithoutResult(s -> {
            Aluno a = alunoRepository.findById(id).orElseThrow();
            a.setNome("Samuel De Marco");
        });

        tx.executeWithoutResult(s -> alunoRepository.deleteById(id));

        tx.executeWithoutResult(s -> {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            List<Number> revisoes = reader.getRevisions(Aluno.class, id);
            assertEquals(3, revisoes.size(), "inclusão, alteração e exclusão");

            assertEquals("Samuel", reader.find(Aluno.class, id, revisoes.get(0)).getNome());
            assertEquals("Samuel De Marco", reader.find(Aluno.class, id, revisoes.get(1)).getNome());

            AuditoriaRevision rev = reader.findRevision(AuditoriaRevision.class, revisoes.get(0));
            assertEquals("administrador", rev.getUsuarioLogin());
            assertTrue(rev.getTimestamp() > 0);
        });
    }

    @Test
    void semUsuarioAutenticado_registraComoSistema() {
        SecurityContextHolder.clearContext(); // ex.: AdminBootstrap no startup

        Long id = tx.execute(s -> {
            Curso c = new Curso();
            c.setNome("Direito");
            return cursoRepository.save(c).getId();
        });

        tx.executeWithoutResult(s -> {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            Number revisao = reader.getRevisions(Curso.class, id).get(0);
            assertEquals("sistema", reader.findRevision(AuditoriaRevision.class, revisao).getUsuarioLogin());
        });
    }

    @Test
    void senhaNaoVaiParaAAuditoria() {
        Long id = tx.execute(s -> {
            Usuario u = new Usuario();
            u.setNome("Operador");
            u.setLogin("operador.auditoria");
            u.setSenha("$2a$10$hashqueNAOdeveSerAuditado");
            u.setCodigoResponsavel(2001);
            u.setAtivo(true);
            u.getPerfis().add(Perfil.OPERADOR);
            return usuarioRepository.save(u).getId();
        });

        tx.executeWithoutResult(s -> {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            Number revisao = reader.getRevisions(Usuario.class, id).get(0);
            Usuario auditado = reader.find(Usuario.class, id, revisao);

            assertEquals("operador.auditoria", auditado.getLogin(), "o usuário é rastreável");
            assertNull(auditado.getSenha(), "o hash da senha não pode ser guardado no histórico");
        });
    }

    @Test
    void historicoStatusNaoEAuditado() {
        // já é append-only (Issue 20): auditá-lo duplicaria a informação
        tx.executeWithoutResult(s -> {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            assertThrows(NotAuditedException.class, () -> reader.getRevisions(HistoricoStatus.class, 1L));
        });
    }
}
