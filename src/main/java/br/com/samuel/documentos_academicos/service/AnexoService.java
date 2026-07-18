package br.com.samuel.documentos_academicos.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import br.com.samuel.documentos_academicos.dto.response.AnexoResponse;
import br.com.samuel.documentos_academicos.entity.SolicitacaoAnexo;

public interface AnexoService {
    AnexoResponse anexar(Long solicitacaoId, MultipartFile arquivo);
    List<AnexoResponse> listar(Long solicitacaoId);
    SolicitacaoAnexo buscarParaDownload(Long solicitacaoId, Long anexoId);
    void excluir(Long solicitacaoId, Long anexoId);
}