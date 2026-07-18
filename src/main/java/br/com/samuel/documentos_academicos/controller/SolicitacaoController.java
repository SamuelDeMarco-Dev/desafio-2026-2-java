package br.com.samuel.documentos_academicos.controller;

import java.net.URI;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.AlteracaoStatusRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoCreateRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.dto.response.ErroResponse;
import br.com.samuel.documentos_academicos.dto.response.HistoricoStatusResponse;
import br.com.samuel.documentos_academicos.dto.response.PageResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResumoResponse;
import br.com.samuel.documentos_academicos.service.SolicitacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Solicitações", description = "Criação, consulta e movimentação de solicitações")
@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @Operation(summary = "Cria uma solicitação",
               description = "O status inicial é sempre ABERTA e as datas são geradas pelo servidor. "
                           + "Exige perfil ADMIN ou OPERADOR.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Criada"),
        @ApiResponse(responseCode = "422", description = "Aluno inativo",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class),
                         examples = @ExampleObject(value = """
                             {
                               "timestamp": "2026-07-16T18:00:00",
                               "status": 422,
                               "erro": "Regra de negócio inválida",
                               "mensagem": "Aluno 1 está inativo e não pode solicitar documentos.",
                               "path": "/api/solicitacoes",
                               "campos": []
                             }"""))),
        @ApiResponse(responseCode = "404", description = "Aluno, curso ou tipo de documento inexistente",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<SolicitacaoResponse> criar(@Valid @RequestBody SolicitacaoCreateRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        SolicitacaoResponse response = solicitacaoService.criar(request);
        URI location = uriBuilder.path("/api/solicitacoes/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Consulta uma solicitação por id",
               description = "Traz os dados completos das entidades relacionadas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrada"),
        @ApiResponse(responseCode = "404", description = "Solicitação inexistente",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}")
    public SolicitacaoResponse buscar(@PathVariable Long id) {
        return solicitacaoService.buscarPorId(id);
    }

    @Operation(summary = "Lista solicitações com filtros e paginação",
               description = "Todos os filtros são opcionais e podem ser combinados; "
                           + "os não informados são ignorados. Sem filtro de `status`, "
                           + "solicitações encerradas ficam fora do resultado. Ordenação "
                           + "padrão: prioridade (URGENTE > ALTA > NORMAL) e depois a mais "
                           + "recente; um `sort` explícito substitui esse padrão.")
    @GetMapping
    public PageResponse<SolicitacaoResumoResponse> listar(
            @ParameterObject SolicitacaoFiltro filtro,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<SolicitacaoResumoResponse> page = solicitacaoService.listar(filtro, pageable);
        return PageResponse.from(page);
    }

    @Operation(summary = "Movimenta a solicitação no fluxo",
               description = """
                       Transições permitidas: ABERTA → EM_ANALISE → APROVADA → EMITIDA, \
                       e EM_ANALISE → REPROVADA. Solicitações finalizadas (EMITIDA, REPROVADA) \
                       não se movimentam. O `codigoResponsavel` informado precisa ser o do \
                       usuário autenticado. A `dataEmissao` só é preenchida ao chegar em EMITIDA.""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimentada"),
        @ApiResponse(responseCode = "403", description = "Responsável incorreto",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Solicitação ou status inexistente",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "409", description = "Alteração concorrente (bloqueio otimista)",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class))),
        @ApiResponse(responseCode = "422", description = "Transição inválida ou solicitação finalizada",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class),
                         examples = @ExampleObject(value = """
                             {
                               "timestamp": "2026-07-16T18:00:00",
                               "status": 422,
                               "erro": "Regra de negócio inválida",
                               "mensagem": "A transição de ABERTA para EMITIDA não é permitida",
                               "path": "/api/solicitacoes/1/status",
                               "campos": []
                             }""")))
    })
    @PatchMapping("/{id}/status")
    public SolicitacaoResponse alterarStatus(@PathVariable Long id,
                                         @Valid @RequestBody AlteracaoStatusRequest request) {
        return solicitacaoService.alterarStatus(id, request);
    }

    @Operation(summary = "Histórico completo de movimentações",
               description = "Em ordem cronológica. A primeira linha é a abertura, com `statusAnterior` nulo. "
                           + "Solicitação sem movimentação devolve lista vazia.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico da solicitação"),
        @ApiResponse(responseCode = "404", description = "Solicitação inexistente",
                     content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/{id}/historico")
    public List<HistoricoStatusResponse> historico(@PathVariable Long id) {
        return solicitacaoService.historico(id);
    }

}