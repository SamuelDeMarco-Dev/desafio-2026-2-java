package br.com.samuel.documentos_academicos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.samuel.documentos_academicos.entity.TipoDocumento;

public interface TipoDocumentoRepository
        extends JpaRepository<TipoDocumento, Long>, JpaSpecificationExecutor<TipoDocumento> {

    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}