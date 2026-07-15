package br.com.samuel.documentos_academicos.service;

import br.com.samuel.documentos_academicos.dto.request.LoginRequest;
import br.com.samuel.documentos_academicos.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse autenticar(LoginRequest request);
}