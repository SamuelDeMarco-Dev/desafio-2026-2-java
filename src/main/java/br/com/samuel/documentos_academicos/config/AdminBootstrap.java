package br.com.samuel.documentos_academicos.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;
import br.com.samuel.documentos_academicos.repository.UsuarioRepository;

/**
 * Cria o administrador inicial no startup, a partir de variáveis de ambiente.
 * É idempotente e nunca registra a senha em log.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String login;
    private final String senha;
    private final Integer codigoResponsavel;

    public AdminBootstrap(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.admin.login}") String login,
                          @Value("${app.admin.senha}") String senha,
                          @Value("${app.admin.codigo-responsavel}") Integer codigoResponsavel) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.login = login;
        this.senha = senha;
        this.codigoResponsavel = codigoResponsavel;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (senha == null || senha.isBlank()) {
            log.warn("ADMIN_PASSWORD não definido — criação do administrador inicial ignorada.");
            return;
        }
        if (usuarioRepository.existsByLogin(login)) {
            log.info("Administrador inicial '{}' já existe — nada a fazer.", login);
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setLogin(login);
        admin.setSenha(passwordEncoder.encode(senha));
        admin.setCodigoResponsavel(codigoResponsavel);
        admin.setAtivo(true);
        admin.setPerfis(Set.of(Perfil.ADMIN));
        usuarioRepository.save(admin);

        log.info("Administrador inicial '{}' criado.", login);
    }
}