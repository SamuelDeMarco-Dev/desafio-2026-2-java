package br.com.samuel.documentos_academicos.service;

import br.com.samuel.documentos_academicos.dto.request.EsqueciSenhaRequest;
import br.com.samuel.documentos_academicos.dto.request.LoginRequest;
import br.com.samuel.documentos_academicos.dto.request.RedefinirSenhaRequest;
import br.com.samuel.documentos_academicos.dto.response.RecuperacaoSenhaResponse;
import br.com.samuel.documentos_academicos.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse autenticar(LoginRequest request);
    RecuperacaoSenhaResponse gerarCodigoRecuperacao(EsqueciSenhaRequest request);
    void redefinirSenha(RedefinirSenhaRequest request);
}