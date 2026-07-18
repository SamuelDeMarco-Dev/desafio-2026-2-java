package br.com.samuel.documentos_academicos.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.samuel.documentos_academicos.dto.response.AnexoResponse;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.SolicitacaoAnexo;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.repository.SolicitacaoAnexoRepository;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.service.AnexoService;

@Service
@Transactional(readOnly = true)
public class AnexoServiceImpl implements AnexoService {

    private final SolicitacaoAnexoRepository anexoRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public AnexoServiceImpl(SolicitacaoAnexoRepository anexoRepository,
                            SolicitacaoRepository solicitacaoRepository) {
        this.anexoRepository = anexoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Override
    @Transactional
    public AnexoResponse anexar(Long solicitacaoId, MultipartFile arquivo) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Solicitação " + solicitacaoId + " não encontrada"));

        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("O arquivo enviado está vazio.");
        }

        SolicitacaoAnexo anexo = new SolicitacaoAnexo();
        anexo.setSolicitacao(solicitacao);
        anexo.setNomeArquivo(nomeSeguro(arquivo.getOriginalFilename()));
        anexo.setTipoConteudo(arquivo.getContentType() != null
                ? arquivo.getContentType() : "application/octet-stream");
        anexo.setTamanhoBytes(arquivo.getSize());
        anexo.setDados(lerBytes(arquivo));
        anexo.setDataUpload(LocalDateTime.now());

        SolicitacaoAnexo salvo = anexoRepository.save(anexo);
        return new AnexoResponse(salvo.getId(), salvo.getNomeArquivo(), salvo.getTipoConteudo(),
                salvo.getTamanhoBytes(), salvo.getDataUpload());
    }

    @Override
    public List<AnexoResponse> listar(Long solicitacaoId) {
        if (!solicitacaoRepository.existsById(solicitacaoId)) {
            throw new RecursoNaoEncontradoException("Solicitação " + solicitacaoId + " não encontrada");
        }
        return anexoRepository.listarPorSolicitacao(solicitacaoId);
    }

    @Override
    public SolicitacaoAnexo buscarParaDownload(Long solicitacaoId, Long anexoId) {
        return anexoRepository.findByIdAndSolicitacaoId(anexoId, solicitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Anexo " + anexoId + " não encontrado na solicitação " + solicitacaoId));
    }

    @Override
    @Transactional
    public void excluir(Long solicitacaoId, Long anexoId) {
        SolicitacaoAnexo anexo = buscarParaDownload(solicitacaoId, anexoId);
        anexoRepository.delete(anexo);
    }

    /** Remove qualquer componente de caminho — só interessa o nome do arquivo. */
    private String nomeSeguro(String nomeOriginal) {
        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            return "arquivo";
        }
        String semCaminho = nomeOriginal.replace("\\", "/");
        return semCaminho.substring(semCaminho.lastIndexOf('/') + 1);
    }

    private byte[] lerBytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler o arquivo enviado.");
        }
    }
}