package br.com.samuel.documentos_academicos.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Solicitações de Documentos Acadêmicos",
                version = "v1",
                description = """
                        Gestão de solicitações de documentos acadêmicos: cadastros, \
                        solicitações, fluxo de status e indicadores.

                        **Como autenticar:** chame `POST /api/auth/login`, copie o campo \
                        `token` da resposta e informe-o no botão **Authorize** acima. \
                        Todos os demais endpoints exigem o token.

                        **Erros:** todas as respostas de erro seguem o mesmo formato \
                        (`timestamp`, `status`, `erro`, `mensagem`, `path`, `campos`).
                        """),
        servers = @Server(url = "/", description = "Servidor atual"),
        security = @SecurityRequirement(name = "bearer-jwt"))
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Token devolvido por POST /api/auth/login. Cole apenas o token, sem o prefixo 'Bearer'.")
public class OpenApiConfig {
}