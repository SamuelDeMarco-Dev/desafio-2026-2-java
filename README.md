# desafio-2026-2-java
CORRESPONDENTE AO EDITAL N. 21/UNOESC-R/2025

Desafio Programador Fullstack Unoesc

Este é o nosso desafio para a vaga de programador Fullstack na Unoesc. Serão testadas as habilidades e qualidade de código ao transformar requisitos limitados em uma aplicação web.

**FAÇA O FORK DESTE REPOSITÓRIO E IMPLEMENTE O DESAFIO. MANTENHA PÚBLICO, POIS QUEREMOS ACOMPANHAR SEUS COMMITS**

Ao concluir o desafio, lembre de enviar um email para recrutamentorh.jba@unoesc.edu.br, ti.coord@unoesc.edu.br e ti.dev@unoesc.edu.br, com seu repositório. Lembre de incluir a documentação para que possamos rodar sua aplicação.

## PONTOS OBRIGATÓRIOS
* Java (21+)
* Orientação a Objetos
* Spring Boot 
* Spring Data JPA
* Maven
* Git
* REST
* PostgreSQL ou Mysql
* Hibernate
* Documentação

## PONTOS DESEJÁVEIS
* Organização em camadas
* Tratamento de exceções
* Thymeleaf ou React
* Testes unitários
* Docker
* OpenAPI/Swagger

## PONTOS DIFERENCIAIS
* Uso de JasperReports para emissão de relatórios.

## AVALIAÇÃO
O código será avaliado de acordo com os seguinte critérios:

* Documentação do processo necessário para rodar a aplicação;
* **Estrutura do projeto;**
* **Histórico do GIT;**
* Build e execução da aplicação;
* Completude das funcionalidades;
* Qualidade de código (design pattern, manutenibilidade, clareza);
* Boas práticas de UI;
* **Sentido e coerência nas respostas aos questionamentos na entrevista de apresentação do desafio realizada pelo candidato.**
 
**OBS: Plágios tendem a ser desclassificados. Atenção com o uso excessivo de IA.**

**IMPORTANTE: Estamos buscando desenvolvedores que topam desafios, então mesmo não cumprindo todo os requisitos abaixo, seu esforço será avaliado.**

## DESAFIO 

Sistema de Gestão de Solicitações de Documentos Acadêmicos

**Contexto**
Uma instituição de ensino deseja automatizar a solicitação de documentos acadêmicos (Histórico Escolar, Atestado de Matrícula, Declaração de Conclusão etc.), abandonando a utilização de documentos impressos.
Para isso foi solicitado o desenvolvimento de uma API REST para receber e movimentar essas solicitações, com um painel de gerenciamento e dashboard de acompanhamento.
Cada solicitação recebida deve possuir um fluxo de aprovação e emissão, com responsáveis por etapa.

**Requisitos Funcionais**
_RF01 – Cadastro de Solicitações [POST]_
Ao criar um novo registro de Solicitação para um Aluno ativo, deve receber as informações:

* id - int
* aluno - Aluno (Entidade)
* curso - Curso (Entidade)
* tipoDocumento - TipoDocumento (Entidade)
* dataSolicitacao - DateTime
* status - Status (Entidade)
* dataAlteracao - DateTime
* prioridade - Enum (URGENTE, ALTA, NORMAL)

_RF01.1 – Cadastro demais Entidades [POST] (**OPCIONAL**)_
Criar endpoints para criação/alteração/remoção das demais entidades Aluno, Curso, TipoDocumento e Status.

_RF02 – Consulta Solicitações [GET]_
Criar endpoints para pesquisar registros de Solicitação contendo filtros:

* Aluno.nome
* Curso.nome
* status
* período
* tipoDocumento.nome
* paginação

Deve ser possivel verificar:

* Quantidade de solicitações por Status
* Quantidade de solicitações por período
* Solicitações realizadas por um Aluno
* Média de tempo até emissão (entre solicitação e emissão)
* Documentos mais solicitados

_RF03 – Alteração de Status [PATCH]_
Uma solicitação deve seguir um fluxo de aprovação de acordo com cada reponsável por uma etapa. Assim, implementar regras para atualizar a Solicitação:

ABERTA -> EM_ANALISE -> APROVADA -> EMITIDA
ABERTA -> EM_ANALISE -> REPROVADA

Não permitir transições inválidas.
Ao alterar o status de uma solicitação, será necessário informar o código do responsável, que deve ser igual ao responsável pelo status passado.
Atualizar a dataAlteracao da Solicitação.

_RF04 - Segurança_
Todos os endpoints existentes devem exigir a passagem de um Token JWT para seu funcionamento, o formato fica a sua escolha.

_RF05 – Auditoria_
Criar tabelas de auditoria para todas as entidades, registrando todas as movimentações realizadas nas mesmas. 

_RF06 – Dashboard_
Montar dashboard onde seja possível visualizar e analisar os dados do RF03.

## ESTRUTURAS PRIMÁRIAS
Aqui uma sugestão de estrutura parcial para você seguir, melhorias ou correções são bem-vindas e encorajadas.

**_Classe SOLICITACAO_**
* "id" - int (auto-gerado)
* "aluno" - Relacionamento com a classe Aluno
* "curso" - Relacionamento com a classe Curso
* "tipo" - Relacionamento com a classe TipoDocumento
* "dataSolicitacao" - DateTime (data criação)
* "dataAlteracao" - DateTime (data última movimentação)
* "status" - Relacionamento com a classe Status
* "prioridade" - Enum

**_Classe ALUNO**
* "id" - int (auto-gerado)
* "nome" - string
* "solicitacoes" - Set de Solicitacao
* "ativo" - boolean

**_Classe CURSO**
* "id" - int (auto-gerado)
* "nome" - string

**_Classe TIPODOCUMENTO**
* "id" - int (auto-gerado)
* "nome" - string

**_Classe STATUS**
* "id" - int (auto-gerado)
* "nome" - string
* "responsavel" - int
* "finalizaSolicitacao" - boolean

**IMPORTANTE: Lembrando que a não completude de todos os pontos, não necessariamente é fator reprovatório, seu esforço será avaliado.**
