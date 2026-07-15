package br.com.samuel.documentos_academicos.service;

import java.util.List;

import br.com.samuel.documentos_academicos.dto.request.StatusRequest;
import br.com.samuel.documentos_academicos.dto.response.StatusResponse;

public interface StatusService {
    StatusResponse criar(StatusRequest request);
    StatusResponse buscarPorId(Long id);
    List<StatusResponse> listar();
    StatusResponse atualizar(Long id, StatusRequest request);
    void excluir(Long id);
}