package br.com.samuel.documentos_academicos;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocumentosAcademicosApplication {

	public static void main(String[] args) {
		// Fixa o fuso da aplicação no horário de Brasília. Sem isto, um container
		// rodando em UTC gravaria as datas (solicitação, alteração, emissão) 3h à
		// frente do horário local, e o front as exibiria erradas.
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
		SpringApplication.run(DocumentosAcademicosApplication.class, args);
	}

}
