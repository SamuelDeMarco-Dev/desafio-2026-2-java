package br.com.samuel.documentos_academicos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.samuel.documentos_academicos.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByLogin(String login);

    /** Usada na autenticação: usuário inativo simplesmente não é encontrado. */
    Optional<Usuario> findByLoginAndAtivoTrue(String login);

    boolean existsByLogin(String login);

    boolean existsByCodigoResponsavel(Integer codigoResponsavel);
}
