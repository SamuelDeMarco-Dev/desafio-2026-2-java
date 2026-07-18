package br.com.samuel.documentos_academicos.service.impl;

import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.service.RelatorioService;
import br.com.samuel.documentos_academicos.specification.SolicitacaoSpecification;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

@Service
@Transactional(readOnly = true)
public class RelatorioServiceImpl implements RelatorioService {

    private static final String TEMPLATE = "/relatorios/solicitacoes.jrxml";
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DATA_CURTA = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    private final SolicitacaoRepository solicitacaoRepository;
    private final Clock clock;

    /** Template compilado uma única vez e reaproveitado (compilar JRXML é caro). */
    private volatile JasperReport templateCompilado;

    public RelatorioServiceImpl(SolicitacaoRepository solicitacaoRepository, Clock clock) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.clock = clock;
    }

    @Override
    public byte[] gerarSolicitacoesPdf(SolicitacaoFiltro filtro) {
        // Mesma Specification da listagem: filtros idênticos aos da tela,
        // incluindo a ordenação por prioridade e a regra de encerradas.
        List<Solicitacao> solicitacoes = solicitacaoRepository
                .findAll(SolicitacaoSpecification.comFiltros(filtro), Pageable.unpaged())
                .getContent();

        List<Map<String, ?>> linhas = new ArrayList<>();
        for (Solicitacao s : solicitacoes) {
            Map<String, Object> linha = new HashMap<>();
            linha.put("id", String.valueOf(s.getId()));
            linha.put("aluno", s.getAluno().getNome());
            linha.put("curso", s.getCurso().getNome());
            linha.put("tipoDocumento", s.getTipoDocumento().getNome());
            linha.put("status", s.getStatus().getNome());
            linha.put("prioridade", s.getPrioridade().name());
            linha.put("dataSolicitacao", s.getDataSolicitacao().format(FORMATO_DATA_CURTA));
            linhas.add(linha);
        }

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("GERADO_EM", LocalDateTime.now(clock).format(FORMATO_DATA_HORA));
        parametros.put("FILTROS", descreverFiltros(filtro));

        try {
            JasperPrint print = JasperFillManager.fillReport(
                    template(), parametros, new JRMapCollectionDataSource(linhas));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar o relatório PDF de solicitações", e);
        }
    }

    private JasperReport template() throws Exception {
        JasperReport compilado = templateCompilado;
        if (compilado == null) {
            synchronized (this) {
                if (templateCompilado == null) {
                    try (InputStream jrxml = getClass().getResourceAsStream(TEMPLATE)) {
                        if (jrxml == null) {
                            throw new IllegalStateException("Template " + TEMPLATE + " não encontrado no classpath");
                        }
                        templateCompilado = JasperCompileManager.compileReport(jrxml);
                    }
                }
                compilado = templateCompilado;
            }
        }
        return compilado;
    }

    private String descreverFiltros(SolicitacaoFiltro filtro) {
        StringJoiner partes = new StringJoiner("; ");
        if (temTexto(filtro.aluno())) partes.add("aluno contém '" + filtro.aluno() + "'");
        if (temTexto(filtro.curso())) partes.add("curso contém '" + filtro.curso() + "'");
        if (temTexto(filtro.tipoDocumento())) partes.add("tipo de documento contém '" + filtro.tipoDocumento() + "'");
        if (temTexto(filtro.status())) partes.add("status = " + filtro.status());
        if (filtro.prioridade() != null) partes.add("prioridade = " + filtro.prioridade());
        if (filtro.dataInicio() != null) partes.add("a partir de " + filtro.dataInicio());
        if (filtro.dataFim() != null) partes.add("até " + filtro.dataFim());

        String descricao = partes.length() > 0 ? partes.toString() : "nenhum";
        if (!temTexto(filtro.status())) {
            descricao += " (solicitações encerradas não incluídas)";
        }
        return descricao;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}