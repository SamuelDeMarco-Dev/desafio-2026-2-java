# Sistema de Gestão de Solicitações de Documentos Acadêmicos

API REST para cadastro, consulta e movimentação de solicitações de documentos
acadêmicos (histórico, diploma, atestado de matrícula, etc.).

> **Status:** em desenvolvimento. Já implementados: infraestrutura (banco,
> migrations, Docker, perfis), o domínio JPA completo, contratos (DTOs) com
> validação, tratamento global de erros, os CRUDs de **alunos, cursos, tipos de
> documento e status**, o **cadastro e a consulta de solicitações** (filtros
> dinâmicos + paginação), os **indicadores do dashboard**, a **autenticação via
> JWT com autorização por perfil**, o **fluxo de movimentação de status** e o
> **histórico de movimentações**. Pendentes: CRUD de usuários via API, auditoria
> e documentação OpenAPI. Veja o [Roadmap](#roadmap).

---

## Tecnologias

### Em uso no projeto

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem |
| Spring Boot | 3.5.16 | Framework base |
| Spring Web | — | API REST |
| Spring Data JPA | — | Persistência |
| Spring Security | — | Autenticação e autorização |
| jjwt | 0.12.6 | Geração e validação de tokens JWT |
| Spring Boot Actuator | — | Health check / observabilidade |
| Spring Boot Validation | — | Validação de dados |
| PostgreSQL | 17 | Banco de dados (produção/dev) |
| Flyway | — | Versionamento do banco (migrations) |
| H2 | — | Banco em memória (testes) |
| Lombok | — | Redução de boilerplate nas entidades |
| Maven | — | Build e dependências |
| Docker / Docker Compose | — | Containerização e orquestração |

### Planejadas (ainda não implementadas)

- OpenAPI / Swagger (documentação da API)
- Testcontainers (testes de integração contra PostgreSQL real)

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
│   │   │   ├── config/                                # Clock, Security, bootstrap do admin
│   │   │   ├── security/                              # JWT (service, filtro), usuário autenticado
│   │   │   ├── controller/                            # endpoints REST
│   │   │   ├── service/  (+ impl/)                    # regras de negócio
│   │   │   ├── repository/                            # Spring Data JPA
│   │   │   ├── specification/                         # consultas dinâmicas (Criteria)
│   │   │   ├── entity/                                # entidades JPA
│   │   │   ├── enums/                                 # Prioridade, CodigoStatus, Perfil
│   │   │   ├── dto/  (request/ e response/)           # contratos da API (records)
│   │   │   ├── mapper/                                # entidade <-> DTO
│   │   │   └── exception/                             # exceções + RestControllerAdvice
│   │   └── resources/
│   │       ├── application.properties                 # config base
│   │       ├── application-dev.properties             # perfil dev
│   │       ├── application-prod.properties            # perfil prod
│   │       └── db/migrations/                         # migrations Flyway
│   │           ├── V1__create_initial_schema.sql
│   │           ├── V2__insert_initial_statuses.sql
│   │           ├── V3__create_usuario_tables.sql
│   │           ├── V4__add_version_to_solicitacao.sql
│   │           └── V5__create_historico_status.sql
│   └── test/
│       ├── java/.../                                  # @WebMvcTest, @DataJpaTest, Mockito
│       └── resources/application-test.properties      # perfil de teste (H2)
├── Dockerfile                                         # build multi-stage
├── docker-compose.yml                                 # PostgreSQL + API
├── .env.example                                       # modelo de variáveis
├── mvnw / mvnw.cmd                                    # Maven Wrapper
└── pom.xml
```

---

## Modelo de dados

O schema é versionado pelo Flyway: `V1` cria a estrutura inicial, `V2` popula os
status do fluxo, `V3` cria as tabelas de usuário, `V4` adiciona o controle de
concorrência na solicitação e `V5` cria o histórico de movimentações.

### Tabelas

| Tabela | Descrição |
|---|---|
| `aluno` | Alunos que solicitam documentos (`nome`, `ativo`) |
| `curso` | Cursos (`nome` único) |
| `tipo_documento` | Tipos de documento emitíveis (`nome` único) |
| `status_solicitacao` | Estados possíveis de uma solicitação |
| `solicitacao` | Solicitação em si, ligando aluno, curso, tipo e status |
| `usuario` | Usuários do sistema (`login` único, senha BCrypt, `codigo_responsavel` único) |
| `usuario_perfil` | Perfis de cada usuário (`ADMIN`, `OPERADOR`, `CONSULTA`) |
| `historico_status` | Movimentações de status (de → para, quando, por quem) |

A tabela `solicitacao` registra `data_solicitacao`, `data_alteracao`,
`data_emissao` e `prioridade` (`URGENTE`, `ALTA` ou `NORMAL`, com constraint de
validação), além de `version` para **bloqueio otimista** — duas movimentações
concorrentes na mesma solicitação resultam em **409**. Todas as chaves
estrangeiras usam `ON DELETE RESTRICT`, e há índices nas colunas mais
consultadas (aluno, curso, tipo, status, data e prioridade).

A `historico_status` é *append-only*: uma linha por movimentação, com
`status_anterior_id` nulo apenas na abertura da solicitação.

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

> Todos os endpoints exigem autenticação, exceto `POST /api/auth/login` e o
> health check. Os corpos de request/response usam DTOs (nunca as entidades
> diretamente) — nenhuma resposta expõe senha ou hash.

### Autenticação — `/api/auth`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/login` | Autentica e devolve o token JWT (**público**) |

```json
{ "login": "administrador", "senha": "sua-senha" }
```

A resposta traz o token, que deve acompanhar as demais chamadas no header
`Authorization: Bearer <token>`.

Detalhes da implementação:

- Senhas são gravadas com **BCrypt**; o login inválido, a senha errada e o
  usuário inativo devolvem a **mesma mensagem genérica**, para não permitir
  enumeração de usuários.
- A sessão é **stateless** — nenhum estado de autenticação fica no servidor.
- O token carrega `sub`, `userId`, `responsavel`, `roles`, `iat` e `exp`; a
  expiração padrão é de 1 hora (`JWT_EXPIRACAO_SEGUNDOS`).
- O administrador inicial é criado no primeiro startup a partir das variáveis de
  ambiente, de forma idempotente. **A senha nunca aparece no código, em migration
  ou em log** — se `ADMIN_PASSWORD` não for definida, a criação é ignorada com um
  aviso.

### Perfis e permissões

| Perfil | Pode |
|---|---|
| `ADMIN` | Tudo: cadastros, exclusões, solicitações e movimentações |
| `OPERADOR` | Criar e movimentar solicitações; consultar tudo |
| `CONSULTA` | Somente leitura |

| Operação | Perfis autorizados |
|---|---|
| `POST` / `PUT` em cadastros e status | `ADMIN` |
| `DELETE` em `/api/**` | `ADMIN` |
| `POST` e `PATCH` em `/api/solicitacoes` | `ADMIN`, `OPERADOR` |
| `GET` em `/api/**` | Qualquer usuário autenticado |

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
| `PATCH` | `/api/solicitacoes/{id}/status` | Movimenta a solicitação no fluxo |
| `GET` | `/api/solicitacoes/{id}/historico` | Histórico completo de movimentações |

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

### Fluxo de status

A movimentação é feita pelo `PATCH /api/solicitacoes/{id}/status`:

```json
{ "statusId": 2, "codigoResponsavel": 1000 }
```

Transições permitidas — qualquer outra é recusada com **422**:

```
ABERTA → EM_ANALISE → APROVADA → EMITIDA
                    ↘ REPROVADA
```

`EMITIDA` e `REPROVADA` são finais: solicitações nesses estados não se movimentam
mais (**422**). Regras aplicadas, nesta ordem:

| Regra | Resposta |
|---|---|
| `codigoResponsavel` informado ≠ o do usuário autenticado | **403** |
| Solicitação já finalizada | **422** |
| Transição fora do fluxo | **422** |
| Status de destino tem responsável e não é o autenticado | **403** |
| Alteração concorrente (bloqueio otimista) | **409** |

Ao aplicar, o servidor atualiza `dataAlteracao` e preenche `dataEmissao`
**somente** quando o destino é `EMITIDA` (em `REPROVADA` ela permanece nula).

### Histórico de movimentações

`GET /api/solicitacoes/{id}/historico` devolve a lista completa, em ordem
cronológica. A primeira linha é sempre a abertura, com `statusAnterior: null`:

```json
[
  {
    "id": 1,
    "statusAnterior": null,
    "statusNovo": { "id": 1, "codigo": "ABERTA", "nome": "Aberta", "responsavel": null, "finalizaSolicitacao": false },
    "responsavel": { "id": 1, "nome": "Administrador", "codigoResponsavel": 1000 },
    "dataMovimentacao": "2026-07-16T16:37:23.456303"
  },
  {
    "id": 2,
    "statusAnterior": { "id": 1, "codigo": "ABERTA", "nome": "Aberta", "responsavel": null, "finalizaSolicitacao": false },
    "statusNovo": { "id": 2, "codigo": "EM_ANALISE", "nome": "Em análise", "responsavel": null, "finalizaSolicitacao": false },
    "responsavel": { "id": 1, "nome": "Administrador", "codigoResponsavel": 1000 },
    "dataMovimentacao": "2026-07-16T16:37:23.596568"
  }
]
```

O registro acontece na **mesma transação** da movimentação: uma tentativa
recusada não deixa rastro no histórico. Solicitação inexistente → **404**;
solicitação sem movimentação → **200** com lista vazia.

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
| Não autenticado, token ausente/inválido/expirado, credenciais inválidas | `401` |
| Autenticado, mas sem permissão (perfil ou responsável incorreto) | `403` |
| Recurso não encontrado | `404` |
| Recurso duplicado (nome/código já existe) ou alteração concorrente | `409` |
| Regra de negócio violada (ex.: exclusão bloqueada, transição inválida) | `422` |
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
| `JWT_SECRET` | Chave de assinatura do token, **mínimo 32 caracteres** | *(defina a sua)* |
| `JWT_EXPIRACAO_SEGUNDOS` | Validade do token | `3600` |
| `ADMIN_LOGIN` | Login do administrador inicial | `administrador` |
| `ADMIN_PASSWORD` | Senha do administrador inicial | *(defina uma senha forte)* |
| `ADMIN_CODIGO_RESPONSAVEL` | Código de responsável do administrador | `1000` |
| `CORS_ORIGENS` | Origens permitidas, separadas por vírgula | `http://localhost:3000` |

Três pontos de segurança que valem atenção:

- **`JWT_SECRET` não tem valor padrão** — a aplicação não sobe sem ela. É
  proposital: uma chave default seria a mesma em qualquer instalação, o que
  permitiria a qualquer um forjar tokens válidos.
- **`ADMIN_PASSWORD` só vem do ambiente.** Sem ela, o administrador não é criado
  (com aviso no log). A senha nunca é gravada em código, migration ou log.
- **`CORS_ORIGENS` nunca deve receber `*`.** Liste as origens explicitamente.

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

São **74 testes**, cobrindo controllers (`@WebMvcTest`), consultas e
Specifications (`@DataJpaTest`), regras de negócio (Mockito) e a segurança
ponta a ponta.

> **Atenção:** o H2 é mais permissivo que o PostgreSQL. Já houve um bug que
> passou por toda a suíte verde e só apareceu no banco real (nulos sem tipo
> enviados como `bytea`). Validar no Docker antes de abrir PR não é opcional —
> é o que a migração para Testcontainers vem resolver.

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

**Milestone 4 — Segurança e fluxo**
- [x] Cadastro de usuários e perfis (senha BCrypt, código de responsável único)
- [x] Autenticação com JWT (login, geração e validação de token)
- [x] Autorização por perfil nos endpoints (`ADMIN`, `OPERADOR`, `CONSULTA`)
- [x] Fluxo de movimentação de status (transições, responsável, datas)
- [x] Histórico de movimentações (status anterior/novo, data, responsável)

**Próximos milestones**
- [ ] CRUD de usuários exposto via API
- [ ] Auditoria (Hibernate Envers)
- [ ] Documentação da API (OpenAPI / Swagger)
- [ ] Testes de integração com Testcontainers (PostgreSQL real)

---

## Licença

Distribuído sob os termos do arquivo [LICENSE](LICENSE).
