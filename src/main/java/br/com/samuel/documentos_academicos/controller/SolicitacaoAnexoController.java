package br.com.samuel.documentos_academicos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.response.AnexoResponse;
import br.com.samuel.documentos_academicos.entity.SolicitacaoAnexo;
import br.com.samuel.documentos_academicos.service.AnexoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Anexos", description = "Documentos anexados às solicitações")
@RestController
@RequestMapping("/api/solicitacoes/{solicitacaoId}/anexos")
public class SolicitacaoAnexoController {

    private final AnexoService anexoService;

    public SolicitacaoAnexoController(AnexoService anexoService) {
        this.anexoService = anexoService;
    }

    @Operation(summary = "Anexa um documento à solicitação",
               description = "Multipart (campo `arquivo`). Exige perfil ADMIN ou OPERADOR.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnexoResponse> anexar(@PathVariable Long solicitacaoId,
                                                @RequestParam("arquivo") MultipartFile arquivo,
                                                UriComponentsBuilder uriBuilder) {
        AnexoResponse response = anexoService.anexar(solicitacaoId, arquivo);
        URI location = uriBuilder.path("/api/solicitacoes/{solicitacaoId}/anexos/{id}")
                .buildAndExpand(solicitacaoId, response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Lista os anexos da solicitação",
               description = "Somente metadados — o conteúdo sai pelo endpoint de download.")
    @GetMapping
    public List<AnexoResponse> listar(@PathVariable Long solicitacaoId) {
        return anexoService.listar(solicitacaoId);
    }

    @Operation(summary = "Baixa o conteúdo de um anexo")
    @GetMapping("/{anexoId}")
    public ResponseEntity<byte[]> baixar(@PathVariable Long solicitacaoId, @PathVariable Long anexoId) {
        SolicitacaoAnexo anexo = anexoService.buscarParaDownload(solicitacaoId, anexoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(anexo.getNomeArquivo(), java.nio.charset.StandardCharsets.UTF_8)
                        .build().toString())
                .contentType(MediaType.parseMediaType(anexo.getTipoConteudo()))
                .body(anexo.getDados());
    }

    @Operation(summary = "Exclui um anexo",
               description = "Exige perfil ADMIN.")
    @DeleteMapping("/{anexoId}")
    public ResponseEntity<Void> excluir(@PathVariable Long solicitacaoId, @PathVariable Long anexoId) {
        anexoService.excluir(solicitacaoId, anexoId);
        return ResponseEntity.noContent().build();
    }
}