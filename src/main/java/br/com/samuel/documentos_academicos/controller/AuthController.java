package br.com.samuel.documentos_academicos.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.samuel.documentos_academicos.dto.request.LoginRequest;
import br.com.samuel.documentos_academicos.dto.response.ErroResponse;
import br.com.samuel.documentos_academicos.dto.response.TokenResponse;
import br.com.samuel.documentos_academicos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Autentica e devolve o token JWT",
            description = "Endpoint público. Login inexistente, senha errada e usuário inativo "
                        + "devolvem a mesma mensagem genérica, para não permitir enumeração de usuários.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticado"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @SecurityRequirements   // vazio = endpoint público, sem cadeado
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.autenticar(request);
    }
}