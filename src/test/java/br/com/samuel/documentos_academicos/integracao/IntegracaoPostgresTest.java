package br.com.samuel.documentos_academicos.integracao;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base dos testes de integração: PostgreSQL real, mesma versão da produção.
 *
 * <p>O container é iniciado num bloco estático e nunca é parado explicitamente —
 * é o padrão <em>singleton</em> do Testcontainers. Com {@code @Container} o JUnit
 * subiria e derrubaria um Postgres por classe de teste; assim sobe um só por JVM,
 * e o Ryuk o remove ao fim da execução.
 *
 * <p>O perfil {@code it} liga o Flyway e mantém o {@code ddl-auto=validate}: cada
 * execução prova que as migrations V1..V6 constroem um schema compatível com as
 * entidades. É a rede que o H2 nunca ofereceu.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
public abstract class IntegracaoPostgresTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}