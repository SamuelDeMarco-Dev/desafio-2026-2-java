# Sistema de Gestão de Solicitações de Documentos Acadêmicos

API REST para cadastro, consulta e movimentação de solicitações de documentos
acadêmicos (histórico, diploma, atestado de matrícula, etc.).

> **Status:** em desenvolvimento. Já implementados: infraestrutura (banco,
> migrations, Docker, perfis), o domínio JPA completo, contratos (DTOs) com
> validação, tratamento global de erros, os CRUDs de **alunos, cursos, tipos de
> documento e status**, o **cadastro e a consulta de solicitações** (filtros
> dinâmicos + paginação) e os **indicadores do dashboard**. Pendentes: segurança
> (JWT), movimentação de status, auditoria e documentação OpenAPI. Veja o
> [Roadmap](#roadmap).

---

## Tecnologias

### Em uso no projeto

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem |
| Spring Boot | 3.5.16 | Framework base |
| Spring Web | — | API REST |
| Spring Data JPA | — | Persistência |
| Spring Boot Actuator | — | Health check / observabilidade |
| Spring Boot Validation | — | Validação de dados |
| PostgreSQL | 17 | Banco de dados (produção/dev) |
| Flyway | — | Versionamento do banco (migrations) |
| H2 | — | Banco em memória (testes) |
| Maven | — | Build e dependências |
| Docker / Docker Compose | — | Containerização e orquestração |

### Planejadas (ainda não implementadas)

- Spring Security + JWT (autenticação e autorização)
- OpenAPI / Swagger (documentação da API)

---

## Estrutura do projeto

Arquitetura em camadas (`controller → service → repository`), com contratos
isolados por DTOs e mapeadores.

```
desafio-2026-2-java/
├── src/
│   ├── main/
│   │   ├── java/br/com/samuel/documentos_academicos/
│   │   │   ├── DocumentosAcademicosApplication.java   # bootstrap Spring Boot
│   │   │   ├── config/                                # beans de configuração (Clock)
│   │   │   ├── controller/                            # endpoints REST
│   │   │   ├── service/  (+ impl/)                    # regras de negócio
│   │   │   ├── repository/                            # Spring Data JPA
│   │   │   ├── specification/                         # consultas dinâmicas (Criteria)
│   │   │   ├── entity/                                # entidades JPA
│   │   │   ├── enums/                                 # Prioridade, CodigoStatus
│   │   │   ├── dto/  (request/ e response/)           # contratos da API (records)
│   │   │   ├── mapper/                                # entidade <-> DTO
│   │   │   └── exception/                             # exceções + RestControllerAdvice
│   │   └── resources/
│   │       ├── application.properties                 # config base
│   │       ├── application-dev.properties             # perfil dev
│   │       ├── application-prod.properties            # perfil prod
│   │       └── db/migrations/                         # migrations Flyway
│   │           ├── V1__create_initial_schema.sql
│   │           └── V2__insert_initial_statuses.sql
│   └── test/
│       ├── java/.../                                  # testes de controller (@WebMvcTest) e mappers
│       └── resources/application-test.properties      # perfil de teste (H2)
├── Dockerfile                                         # build multi-stage
├── docker-compose.yml                                 # PostgreSQL + API
├── .env.example                                       # modelo de variáveis
├── mvnw / mvnw.cmd                                    # Maven Wrapper
└── pom.xml
```

---

## Modelo de dados

O schema é versionado pelo Flyway. A migration `V1` cria a estrutura inicial e a
`V2` popula os status do fluxo.

### Tabelas

| Tabela | Descrição |
|---|---|
| `aluno` | Alunos que solicitam documentos (`nome`, `ativo`) |
| `curso` | Cursos (`nome` único) |
| `tipo_documento` | Tipos de documento emitíveis (`nome` único) |
| `status_solicitacao` | Estados possíveis de uma solicitação |
| `solicitacao` | Solicitação em si, ligando aluno, curso, tipo e status |

A tabela `solicitacao` registra `data_solicitacao`, `data_alteracao`,
`data_emissao` e `prioridade` (`URGENTE`, `ALTA` ou `NORMAL`, com constraint de
validação). Todas as chaves estrangeiras usam `ON DELETE RESTRICT`, e há índices
nas colunas mais consultadas (aluno, curso, tipo, status, data e prioridade).

### Status iniciais (seed da V2)

| Código | Nome | Finaliza solicitação |
|---|---|---|
| `ABERTA` | Aberta | Não |
| `EM_ANALISE` | Em análise | Não |
| `APROVADA` | Aprovada | Não |
| `EMITIDA` | Emitida | Sim |
| `REPROVADA` | Reprovada | Sim |

---

## API REST

> Todos os endpoints de negócio ainda são públicos — a proteção por JWT entra no
> milestone de segurança. Os corpos de request/response usam DTOs (nunca as
> entidades diretamente).

### Alunos — `/api/alunos`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/alunos` | Cadastra aluno (`nome` obrigatório) → **201** + `Location` |
| `GET` | `/api/alunos` | Lista **paginada**; filtros opcionais `?nome=` e `?ativo=` |
| `GET` | `/api/alunos/{id}` | Consulta por id |
| `GET` | `/api/alunos/{id}/solicitacoes` | Solicitações do aluno (**paginado**) |
| `PUT` | `/api/alunos/{id}` | Atualiza o nome |
| `PATCH` | `/api/alunos/{id}/ativo` | Ativa/inativa (corpo `{ "ativo": false }`) |
| `DELETE` | `/api/alunos/{id}` | Remove (bloqueado se houver solicitações vinculadas → **422**) |

### Cursos — `/api/cursos`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/cursos` | Cadastra curso; **nome único** (duplicado → **409**) |
| `GET` | `/api/cursos` | Lista paginada; filtro opcional `?nome=` |
| `GET` | `/api/cursos/{id}` | Consulta por id |
| `PUT` | `/api/cursos/{id}` | Atualiza (mantém unicidade do nome) |
| `DELETE` | `/api/cursos/{id}` | Remove (bloqueado se vinculado a solicitações → **422**) |

### Tipos de documento — `/api/tipos-documento`

Mesmo contrato dos cursos (nome único, listagem paginada, integridade
referencial no *delete*): `POST`, `GET`, `GET/{id}`, `PUT/{id}`, `DELETE/{id}`.

### Status — `/api/status`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/status` | Cadastra status; **código único**; `finalizaSolicitacao` coerente com o fluxo |
| `GET` | `/api/status` | Lista todos (tabela de referência, sem paginação) |
| `GET` | `/api/status/{id}` | Consulta por id |
| `PUT` | `/api/status/{id}` | Atualiza; estruturais têm código e finalização imutáveis |
| `DELETE` | `/api/status/{id}` | Remove; **estruturais e vinculados são bloqueados** → **422** |

Os status estruturais (`ABERTA`, `EM_ANALISE`, `APROVADA`, `EMITIDA`,
`REPROVADA`) não podem ser excluídos nem ter o código/finalização alterados.

### Solicitações — `/api/solicitacoes`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/solicitacoes` | Cria solicitação → **201** + `Location` |
| `GET` | `/api/solicitacoes` | Lista **paginada** com filtros dinâmicos (resumo) |
| `GET` | `/api/solicitacoes/{id}` | Consulta detalhada (dados das entidades relacionadas) |

**Criação** — o cliente informa apenas as referências e a prioridade:

```json
{
  "alunoId": 1,
  "cursoId": 1,
  "tipoDocumentoId": 1,
  "prioridade": "NORMAL"
}
```

Regras aplicadas pelo servidor:

- O aluno deve existir e estar **ativo** (inativo → **422**); referências
  inexistentes → **404**.
- O **status inicial é sempre `ABERTA`** — o cliente não pode escolhê-lo.
- `dataSolicitacao` e `dataAlteracao` são geradas pelo servidor (via um `Clock`
  injetável, o que mantém as datas testáveis); `dataEmissao` inicia nula.
- `prioridade` é opcional e assume `NORMAL` quando omitida.

**Filtros da listagem** (todos opcionais, funcionam isolados ou combinados; os
não informados são ignorados):

| Parâmetro | Efeito |
|---|---|
| `aluno` | Nome do aluno (parcial, ignora maiúsculas/minúsculas) |
| `curso` | Nome do curso (parcial, ignora caixa) |
| `tipoDocumento` | Nome do tipo de documento (parcial, ignora caixa) |
| `status` | Código do status (ex.: `ABERTA`) |
| `prioridade` | `URGENTE`, `ALTA` ou `NORMAL` |
| `dataInicio` / `dataFim` | Intervalo de `dataSolicitacao` (ISO `yyyy-MM-dd`, **fim inclusivo**) |

Exemplo:

```
GET /api/solicitacoes?aluno=Samuel&status=ABERTA&dataInicio=2026-07-01&dataFim=2026-07-31&page=0&size=20&sort=dataSolicitacao,desc
```

### Dashboard — `/api/dashboard`

Indicadores agregados. Todos aceitam o filtro de período opcional
`?dataInicio=&dataFim=`.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/dashboard/resumo` | Total no período + contagem por status + tempo médio |
| `GET` | `/api/dashboard/solicitacoes-por-status` | Quantidade por status (maior primeiro) |
| `GET` | `/api/dashboard/documentos-mais-solicitados` | Ranking de tipos de documento |
| `GET` | `/api/dashboard/tempo-medio-emissao` | Tempo médio entre solicitação e emissão |

O **tempo médio considera apenas solicitações emitidas** (com `dataEmissao`
preenchida) e é retornado em dias (fracionário):

```json
{ "diasMedios": 2.5, "totalEmitidas": 4 }
```

As consultas de indicadores usam agregações no banco projetadas diretamente em
DTOs — não carregam entidades completas.

### Paginação

As listagens paginadas aceitam `?page=`, `?size=` e `?sort=` (ex.:
`?page=0&size=20&sort=nome,asc`) e respondem num envelope padronizado:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

### Formato de erro

Todos os erros seguem um formato único, produzido por um
`@RestControllerAdvice` global:

```json
{
  "timestamp": "2026-07-14T18:00:00",
  "status": 422,
  "erro": "Regra de negócio inválida",
  "mensagem": "Curso está vinculado a solicitações e não pode ser removido.",
  "path": "/api/cursos/1",
  "campos": []
}
```

| Situação | HTTP |
|---|---|
| Validação de campos (`campos` preenchido) / JSON malformado | `400` |
| Recurso não encontrado | `404` |
| Recurso duplicado (nome/código já existe) | `409` |
| Regra de negócio violada (ex.: exclusão bloqueada) | `422` |
| Erro inesperado (sem *stack trace*, com código de correlação no log) | `500` |

---

## Perfis de ambiente

| Perfil | Banco | `ddl-auto` | Flyway | Uso |
|---|---|---|---|---|
| `dev` | PostgreSQL | `validate` | habilitado | Desenvolvimento (padrão) |
| `prod` | PostgreSQL | `validate` | habilitado | Produção |
| `test` | H2 (em memória) | `create-drop` | desabilitado | Testes automatizados |

Em `dev` e `prod`, o Hibernate apenas **valida** o schema contra o banco — quem
cria e evolui a estrutura é exclusivamente o Flyway.

---

## Variáveis de ambiente

Copie `.env.example` para `.env` e ajuste os valores (o `.env` é ignorado pelo
Git):

```bash
cp .env.example .env
```

| Variável | Descrição | Exemplo |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil ativo | `dev` |
| `DB_NAME` | Nome do banco | `documentos_academicos` |
| `DB_USERNAME` | Usuário do banco | `documentos_app` |
| `DB_PASSWORD` | Senha do banco | *(defina a sua)* |
| `DB_URL` | JDBC URL para execução local (fora do Docker) | `jdbc:postgresql://localhost:5432/documentos_academicos` |
| `SERVER_PORT` | Porta HTTP da API | `8080` |
| `DB_PORT` | Porta do PostgreSQL exposta pelo host | `5433` |

---

## Como executar

### Com Docker (recomendado)

Sobe o PostgreSQL e a API juntos:

```bash
docker compose up --build
```

- API disponível em `http://localhost:8080`
- PostgreSQL exposto na porta `5433` do host (5432 dentro da rede do Docker)

O serviço `api` só inicia após o PostgreSQL passar no *health check*, e as
migrations do Flyway são aplicadas automaticamente no startup da aplicação.

Para parar e remover também o volume do banco (recomeço limpo):

```bash
docker compose down -v
```

### Localmente (sem Docker)

Requer um PostgreSQL rodando e acessível via `DB_URL`, além de Java 21.

```bash
./mvnw spring-boot:run
```

---

## Testes

Os testes usam o perfil `test` com banco H2 em memória (Flyway desabilitado):

```bash
./mvnw test
```

---

## Health check

Com a aplicação no ar, o estado de saúde é exposto pelo Actuator:

```
GET http://localhost:8080/actuator/health
```

Em `dev`, o endpoint mostra detalhes (`show-details=always`); em `prod`, apenas
o status geral.

---

## Roadmap

**Milestone 1 — Infraestrutura**
- [x] Bootstrap do projeto (Java 21 + Spring Boot + Maven)
- [x] Conexão com PostgreSQL e perfis de ambiente (dev/prod/test)
- [x] Ambiente Docker (API + PostgreSQL via Docker Compose)
- [x] Versionamento do banco com Flyway (schema inicial + status)
- [x] Health check via Actuator

**Milestone 2 — Domínio e cadastros**
- [x] Entidades JPA e enums do domínio
- [x] DTOs (records) com validação e mapeadores
- [x] Tratamento global de erros (`@RestControllerAdvice` + formato único)
- [x] CRUD de alunos (com paginação e inativação)
- [x] CRUD de cursos (nome único)
- [x] CRUD de tipos de documento (nome único)
- [x] Gerenciamento de status (fluxo estrutural preservado)

**Milestone 3 — Solicitações e consultas**
- [x] Cadastro de solicitações (aluno ativo, status `ABERTA` e datas pelo servidor)
- [x] Consulta detalhada e listagem paginada com filtros dinâmicos (Specification)
- [x] Indicadores do dashboard (por status, ranking de documentos, tempo médio)
- [x] Solicitações por aluno (paginado)

**Próximos milestones**
- [ ] Autenticação e autorização (Spring Security + JWT)
- [ ] Fluxo e alteração de status das solicitações
- [ ] Auditoria (Hibernate Envers)
- [ ] Documentação da API (OpenAPI / Swagger)
- [ ] Testes de integração dos endpoints

---

## Licença

Distribuído sob os termos do arquivo [LICENSE](LICENSE).
