package br.com.samuel.documentos_academicos.service;

import br.com.samuel.documentos_academicos.dto.request.UsuarioRequest;
import br.com.samuel.documentos_academicos.dto.response.UsuarioResponse;

public interface UsuarioService {
    UsuarioResponse criar(UsuarioRequest request);
    UsuarioResponse buscarPorId(Long id);
    UsuarioResponse alterarSituacao(Long id, boolean ativo);
}