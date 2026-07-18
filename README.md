# Sistema de Gestão de Solicitações de Documentos Acadêmicos

Aplicação full-stack para cadastro, consulta e movimentação de solicitações de
documentos acadêmicos (histórico, diploma, atestado de matrícula, etc.):
**API REST** em Java/Spring Boot e **interface web** em React/TypeScript.

> **Status: funcional de ponta a ponta.** Backend: CRUDs completos (alunos,
> cursos, tipos de documento, status, usuários), solicitações com filtros
> dinâmicos, fluxo de status com histórico, **anexos de documentos**,
> autenticação JWT com **recuperação de senha**, auditoria (Hibernate Envers),
> **relatórios PDF** (JasperReports), Swagger, testes unitários + integração
> (Testcontainers) e análise estática. Frontend: login, gestão de solicitações
> (com anexos e visualização do fluxo em diagrama), dashboard de indicadores,
> telas de cadastro, tema claro/escuro e geração de PDF pela interface. Veja o
> [Roadmap](#roadmap).

**Sumário rápido:** [Pré-requisitos](#pré-requisitos) ·
[Variáveis de ambiente](#variáveis-de-ambiente) ·
[Como executar](#como-executar) · [Testes](#testes) ·
[Swagger](#documentação-interativa-swagger) ·
[Fluxo de status](#fluxo-de-status) ·
[Limitações e decisões técnicas](#limitações-conhecidas-e-decisões-técnicas)

> 📘 **Guia ilustrado (PDF):** para um passo a passo enxuto e com espaço para os
> prints das telas, veja [`docs/GUIA-INSTALACAO-E-USO.pdf`](docs/GUIA-INSTALACAO-E-USO.pdf).
> Para regenerá-lo depois de adicionar as imagens em [`docs/imagens/`](docs/imagens/),
> rode `powershell -ExecutionPolicy Bypass -File docs\gerar-pdf.ps1` (usa o Edge/Chrome já
> instalado, sem dependências extras).

---

## Tecnologias

### Em uso no projeto

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem |
| Spring Boot | 3.5.16 | Framework base |
| Spring Web | — | API REST |
| Spring Data JPA | — | Persistência |
| Hibernate Envers | — | Auditoria das entidades |
| Spring Security | — | Autenticação e autorização |
| jjwt | 0.12.6 | Geração e validação de tokens JWT |
| springdoc-openapi | 2.8.9 | Documentação interativa (Swagger UI) |
| Spring Boot Actuator | — | Health check / observabilidade |
| Spring Boot Validation | — | Validação de dados |
| PostgreSQL | 17 | Banco de dados (produção/dev) |
| Flyway | — | Versionamento do banco (migrations) |
| H2 | — | Banco em memória (testes unitários) |
| Testcontainers | — | PostgreSQL real nos testes de integração |
| JasperReports | 6.21.5 | Relatórios PDF |
| SpotBugs | 4.8.6.6 | Análise estática (roda no `verify`) |
| Lombok | — | Redução de boilerplate nas entidades |
| Maven | — | Build e dependências |
| Docker / Docker Compose | — | Containerização e orquestração |
| React | 19 | Interface web (SPA) |
| TypeScript | 6 | Tipagem estática do frontend |
| Vite | 8 | Dev server e build do frontend |
| react-router-dom | 7 | Roteamento da SPA |
| Axios | 1.x | Cliente HTTP (interceptors de token/401) |
| Recharts | 3.x | Gráficos do dashboard |

---

## Pré-requisitos

### Para rodar com Docker (recomendado)

| Requisito | Versão mínima | Verificação |
|---|---|---|
| Docker + Docker Compose | Docker 24+ (Compose v2) | `docker compose version` |
| Node.js + npm (para o frontend) | Node 20+ | `node --version` |
| Git | qualquer recente | `git --version` |

> O **backend não exige Java instalado** neste modo: o build acontece dentro da
> imagem (multi-stage). O frontend roda no host com o dev server do Vite.

### Para rodar sem Docker

| Requisito | Versão mínima | Verificação |
|---|---|---|
| JDK | 21 | `java --version` |
| PostgreSQL | 17 (recomendado; 15+ funciona) | `psql --version` |
| Node.js + npm | Node 20+ | `node --version` |

O Maven não precisa ser instalado — o projeto inclui o wrapper (`./mvnw` /
`mvnw.cmd`). Os testes de integração (`./mvnw verify`) exigem Docker no ar em
**ambos** os modos, porque usam Testcontainers.

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
│   │   │   ├── config/                                # Clock, Security, OpenAPI, bootstrap do admin
│   │   │   ├── security/                              # JWT (service, filtro), usuário autenticado
│   │   │   ├── audit/                                 # RevisionListener do Envers
│   │   │   ├── controller/                            # endpoints REST
│   │   │   ├── service/  (+ impl/)                    # regras de negócio
│   │   │   ├── repository/                            # Spring Data JPA
│   │   │   ├── specification/                         # consultas dinâmicas (Criteria)
│   │   │   ├── entity/                                # entidades JPA
│   │   │   ├── enums/                                 # Prioridade, CodigoStatus, Perfil
│   │   │   ├── dto/  (request/, response/)            # contratos da API (records)
│   │   │   │   └── projection/                        # projeções de consulta (não são API)
│   │   │   ├── mapper/                                # entidade <-> DTO
│   │   │   └── exception/                             # exceções + RestControllerAdvice
│   │   └── resources/
│   │       ├── application.properties                 # config base
│   │       ├── application-dev.properties             # perfil dev
│   │       ├── application-prod.properties            # perfil prod
│   │       ├── relatorios/solicitacoes.jrxml          # template JasperReports (PDF)
│   │       └── db/migrations/                         # migrations Flyway
│   │           ├── V1__create_initial_schema.sql
│   │           ├── V2__insert_initial_statuses.sql
│   │           ├── V3__create_usuario_tables.sql
│   │           ├── V4__add_version_to_solicitacao.sql
│   │           ├── V5__create_historico_status.sql
│   │           ├── V6__create_audit_tables.sql
│   │           ├── V7__create_solicitacao_anexo.sql
│   │           └── V8__add_recuperacao_senha_usuario.sql
│   └── test/
│       ├── java/.../                                  # @WebMvcTest, @DataJpaTest, Mockito
│       └── resources/application-test.properties      # perfil de teste (H2)
├── frontend/                                          # SPA React + TypeScript (Vite)
│   ├── src/
│   │   ├── auth/                                      # sessão, contexto, guardas de rota, permissões
│   │   ├── components/                                # ChipSelect, Modal, BPMN, anexos, voltar
│   │   ├── layouts/MainLayout.tsx                     # sidebar + topbar
│   │   ├── pages/                                     # login, dashboard, solicitações, cadastros
│   │   ├── services/                                  # cliente Axios + APIs tipadas por recurso
│   │   └── tema/                                      # modo claro/escuro persistido
│   ├── .env.example                                   # modelo (VITE_API_URL)
│   └── package.json
├── Dockerfile                                         # build multi-stage (backend)
├── docker-compose.yml                                 # PostgreSQL + API
├── .env.example                                       # modelo de variáveis (backend)
├── mvnw / mvnw.cmd                                    # Maven Wrapper
└── pom.xml
```

---

## Modelo de dados

O schema é versionado pelo Flyway: `V1` cria a estrutura inicial, `V2` popula os
status do fluxo, `V3` cria as tabelas de usuário, `V4` adiciona o controle de
concorrência na solicitação, `V5` cria o histórico de movimentações, `V6` cria
as tabelas de auditoria, `V7` cria os anexos das solicitações e `V8` adiciona as
colunas de recuperação de senha.

> Como o `ddl-auto` é `validate`, **o Flyway precisa criar até as tabelas do
> Envers** — o Hibernate valida o schema de auditoria junto com o resto e não
> sobe se faltar alguma coluna.

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
| `solicitacao_anexo` | Documentos anexados à solicitação (conteúdo em `bytea`, máx. 10MB) |
| `revinfo` | Uma linha por transação auditada (revisão, data, login do autor) |
| `<tabela>_aud` | Versões de cada entidade auditada (7 tabelas) |

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

> Todos os endpoints exigem autenticação, exceto os de `/api/auth` (login e
> recuperação de senha), o health check e a documentação. Os corpos de
> request/response usam DTOs (nunca as entidades diretamente) — nenhuma resposta
> expõe senha ou hash.

### Documentação interativa (Swagger)

Com a aplicação no ar em `dev`:

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Documento OpenAPI (JSON) | `http://localhost:8080/v3/api-docs` |

Para testar endpoints protegidos pela interface:

1. Abra `POST /api/auth/login`, clique em **Try it out** e envie suas credenciais.
2. Copie o valor de `token` da resposta.
3. Clique em **Authorize** no topo da página, cole o token (**sem** o prefixo
   `Bearer`) e confirme.

Todas as operações têm descrição, e as respostas de erro apontam para o schema
`ErroResponse` — não para o schema de sucesso. Um teste automatizado varre o
documento e falha se algum endpoint ficar sem descrição.

> **A documentação é desabilitada no perfil `prod`** (`springdoc.*.enabled=false`).
> Um console interativo que ensina a autenticar e permite disparar requisições é
> superfície de ataque desnecessária em produção; lá as duas URLs devolvem `404`.

### Autenticação — `/api/auth`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/auth/login` | Autentica e devolve o token JWT (**público**) |
| `POST` | `/api/auth/esqueci-senha` | Gera código de recuperação de senha (**público**) |
| `POST` | `/api/auth/redefinir-senha` | Redefine a senha com o código (**público**) |

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

**Recuperação de senha** (autosserviço): `POST /api/auth/esqueci-senha` com
`{ "login": "..." }` gera um código de **6 dígitos**, válido por **15 minutos** e
de **uso único** (apenas o hash BCrypt fica no banco). Em seguida,
`POST /api/auth/redefinir-senha` com `{ "login", "codigo", "novaSenha" }` troca a
senha (mínimo 8 caracteres). Código errado, expirado ou reutilizado → **422**;
login inexistente ou inativo recebe a resposta genérica, sem código.

> **Limitação assumida:** o projeto não tem serviço de e-mail, então o código
> volta **na própria resposta** do `esqueci-senha` (campo `codigoRecuperacao`).
> Em produção esse campo viria nulo e o código seguiria por e-mail — a
> arquitetura (hash + validade + uso único) já é a definitiva; só o canal de
> entrega é simulado.

### Perfis e permissões

| Perfil | Pode |
|---|---|
| `ADMIN` | Tudo: cadastros, exclusões, solicitações e movimentações |
| `OPERADOR` | Criar e movimentar solicitações; consultar tudo |
| `CONSULTA` | Somente leitura |

| Operação | Perfis autorizados |
|---|---|
| `POST` / `PUT` / `PATCH` em cadastros (alunos, cursos, tipos, status, usuários) | `ADMIN` |
| `DELETE` em `/api/**` | `ADMIN` |
| `POST` e `PATCH` em `/api/solicitacoes` (inclui anexos) | `ADMIN`, `OPERADOR` |
| `GET` em `/api/**` (inclui relatórios PDF) | Qualquer usuário autenticado |

> Na **interface web**, a seção Cadastros só aparece (e só é navegável) para
> `ADMIN`. Isso é conveniência de UI — a regra que vale é a do backend acima,
> aplicada mesmo se alguém chamar a API diretamente.

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

### Usuários — `/api/usuarios`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/usuarios` | Cadastra usuário (login e código de responsável **únicos**) |
| `GET` | `/api/usuarios` | Lista paginada; filtros opcionais `?nome=` e `?ativo=` |
| `GET` | `/api/usuarios/{id}` | Consulta por id |
| `PUT` | `/api/usuarios/{id}` | Atualiza nome, código de responsável e perfis (**não** altera login/senha) |
| `PATCH` | `/api/usuarios/{id}/ativo` | Ativa/inativa (inativo não consegue autenticar) |

Não há `DELETE` de usuário — por design, contas são inativadas, nunca removidas
(preserva a integridade do histórico de movimentações e da auditoria). Nenhuma
resposta expõe senha ou hash; a criação exige senha de 8 a 72 caracteres e ao
menos um perfil.

### Solicitações — `/api/solicitacoes`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/solicitacoes` | Cria solicitação → **201** + `Location` |
| `GET` | `/api/solicitacoes` | Lista **paginada** com filtros dinâmicos (resumo) |
| `GET` | `/api/solicitacoes/{id}` | Consulta detalhada (dados das entidades relacionadas) |
| `PATCH` | `/api/solicitacoes/{id}/status` | Movimenta a solicitação no fluxo |
| `GET` | `/api/solicitacoes/{id}/historico` | Histórico completo de movimentações |
| `POST` | `/api/solicitacoes/{id}/anexos` | Anexa um documento (multipart, campo `arquivo`, máx. **10MB**) |
| `GET` | `/api/solicitacoes/{id}/anexos` | Lista os anexos (só metadados) |
| `GET` | `/api/solicitacoes/{id}/anexos/{anexoId}` | Baixa o conteúdo do anexo |
| `DELETE` | `/api/solicitacoes/{id}/anexos/{anexoId}` | Exclui o anexo (`ADMIN`) |

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

**Comportamento padrão da listagem** (sem `?sort=` explícito):

- **Ordenação por prioridade** — `URGENTE` → `ALTA` → `NORMAL` e, dentro da
  mesma prioridade, a mais recente primeiro. Como a prioridade é gravada como
  texto, a ordem vem de um `CASE` na consulta (ordenar a coluna daria ordem
  alfabética).
- **Solicitações encerradas ficam fora** — sem filtro de `status`, as que estão
  em status finalizador (`EMITIDA`, `REPROVADA`) não aparecem; para vê-las,
  filtre pelo status explicitamente (ex.: `?status=EMITIDA`).
- Um `?sort=` explícito substitui a ordenação padrão.

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

Detalhes que sustentam o fluxo:

- As transições são definidas no enum `CodigoStatus.transicoesPermitidas()` —
  código, não configuração: mudar o fluxo exige mudar (e testar) o enum.
- Um status pode ter um **responsável** (`responsavel` na tabela): quando
  preenchido, só o usuário com aquele `codigoResponsavel` movimenta a
  solicitação **para** aquele status.
- Status **customizados** podem ser criados via `/api/status`, mas não entram
  nas transições do fluxo estrutural — servem para relatório/consulta.
- Na **interface web**, o botão "Ver fluxo" (no detalhe e na listagem) abre um
  diagrama estilo BPMN com a etapa atual destacada; a tela também esconde o
  formulário de movimentação em solicitações finalizadas e marca como
  "(restrito)" os destinos com responsável de outro usuário — sempre como
  conveniência: a validação que vale é a do servidor.

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

### Relatórios PDF — `/api/relatorios`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/relatorios/solicitacoes` | Relatório PDF das solicitações (download) |

Gerado com **JasperReports** a partir do template
`src/main/resources/relatorios/solicitacoes.jrxml` (compilado uma única vez e
reaproveitado). Aceita **exatamente os mesmos filtros da listagem** — a mesma
`Specification` é reutilizada, então o PDF reflete o que a tela mostra,
incluindo a ordenação por prioridade e a regra de encerradas. O cabeçalho do
relatório imprime a data de geração e os filtros aplicados; o rodapé traz o
total.

```bash
# exemplo com curl (token obtido no login)
curl -o relatorio.pdf "http://localhost:8080/api/relatorios/solicitacoes?status=EMITIDA" \
     -H "Authorization: Bearer $TOKEN"
```

Na interface, o botão **"Gerar PDF"** na tela de Solicitações baixa o relatório
com os filtros aplicados no momento.

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
| Recurso não encontrado, ou rota inexistente | `404` |
| Recurso duplicado (nome/código já existe) ou alteração concorrente | `409` |
| Regra de negócio violada (ex.: exclusão bloqueada, transição inválida) | `422` |
| Erro inesperado (sem *stack trace*, com código de correlação no log) | `500` |

---

## Auditoria

Toda inclusão, alteração e exclusão das entidades principais é registrada pelo
**Hibernate Envers**, com o usuário responsável e a data. Não há endpoint: a
auditoria é um rastro técnico, consultado via SQL ou pela API `AuditReader` do
Envers. Expor esse histórico via HTTP é uma decisão de produto com implicações
próprias de segurança (quem pode ver o histórico de quem?) e ficou fora do escopo.

**Entidades auditadas:** `Aluno`, `Curso`, `TipoDocumento`, `Status`,
`Solicitacao` e `Usuario`.

`HistoricoStatus` **não** é auditada: ela já é *append-only* e nunca sofre
update, então auditá-la produziria um histórico do histórico. Note que as duas
coisas não competem — o histórico da solicitação é memória **de negócio** ("por
que esta solicitação foi reprovada"), a auditoria é rastro **técnico** ("quem
renomeou aquele curso na terça").

Como consultar o rastro de um aluno:

```sql
SELECT r.rev, r.usuario_login, to_timestamp(r.revtstmp/1000) AS quando,
       a.id, a.nome, a.revtype
FROM revinfo r JOIN aluno_aud a ON a.rev = r.rev
ORDER BY r.rev;
```

```
 rev | usuario_login |       quando        | id |           nome            | revtype
-----+---------------+---------------------+----+---------------------------+---------
   1 | administrador | 2026-07-16 23:59:37 |  3 | Aluno Auditoria           |       0
   2 | administrador | 2026-07-16 23:59:37 |  3 | Aluno Auditoria Renomeado |       1
   3 | administrador | 2026-07-16 23:59:37 |  3 | Aluno Auditoria Renomeado |       2
```

`revtype`: **0** = inclusão, **1** = alteração, **2** = exclusão. O `revtstmp` é
epoch em milissegundos — daí o `to_timestamp` acima.

Decisões que valem conhecer:

- **A senha nunca é auditada** (`@NotAudited`). Sem isso, a `usuario_aud`
  acumularia todo hash BCrypt que o usuário já teve — um passivo de segurança sem
  contrapartida. O `version` da solicitação também fica de fora: é contador
  interno de concorrência, só geraria ruído.
- **A exclusão preserva os dados** (`store_data_at_delete=true`). Sem isso a
  linha de DELETE guardaria apenas o id e nulos: você saberia que o registro
  sumiu, mas não o que sumiu.
- **Escritas fora de requisição são registradas como `sistema`** — o caso do
  `AdminBootstrap` no startup, que roda sem usuário autenticado.
- **Não há auditoria retroativa.** Registros criados antes da `V6` não têm
  revisão; o Envers só enxerga o que passa por ele.

---

## Perfis de ambiente

| Perfil | Banco | `ddl-auto` | Flyway | Uso |
|---|---|---|---|---|
| `dev` | PostgreSQL | `validate` | habilitado | Desenvolvimento (padrão) |
| `prod` | PostgreSQL | `validate` | habilitado | Produção; Swagger desligado |
| `test` | H2 (em memória) | `create-drop` | desabilitado | Testes unitários (`./mvnw test`) |
| `it` | PostgreSQL (Testcontainers) | `validate` | habilitado | Testes de integração (`./mvnw verify`) |

Em `dev`, `prod` e `it`, o Hibernate apenas **valida** o schema contra o banco —
quem cria e evolui a estrutura é exclusivamente o Flyway. O perfil `it` existe
justamente para que essa combinação seja exercitada por teste, e não só quando a
aplicação sobe: é ela que pega uma migration divergente das entidades.

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

### Frontend

O frontend tem seu próprio arquivo de ambiente. Copie o modelo (**execute o
comando** — não cole o texto dele dentro do arquivo):

```bash
cd frontend
cp .env.example .env      # Windows (PowerShell): Copy-Item .env.example .env
```

| Variável | Descrição | Exemplo |
|---|---|---|
| `VITE_API_URL` | URL base da API para o navegador | `http://localhost:8080` |

O cliente HTTP falha na inicialização, com mensagem explícita, se `VITE_API_URL`
não estiver definida — melhor que erros de rede silenciosos apontando para
`undefined`.

---

## Como executar

O caminho completo, do clone ao navegador. Os comandos assumem o terminal na
raiz do repositório; no Windows, use `mvnw.cmd` no lugar de `./mvnw` (os
comandos `docker`, `npm` e `cp`/`Copy-Item` valem igual).

### Passo 0 — preparar as variáveis (uma única vez)

```bash
git clone <url-do-repositorio> && cd desafio-2026-2-java

# backend
cp .env.example .env
# edite o .env: defina DB_PASSWORD, JWT_SECRET (>= 32 caracteres) e ADMIN_PASSWORD

# frontend
cd frontend && cp .env.example .env && cd ..
```

> Sem `JWT_SECRET` a API **não sobe**; sem `ADMIN_PASSWORD` ela sobe, mas **sem
> nenhum usuário** — defina os dois antes do primeiro start.

### Opção A — backend com Docker (recomendado)

Sobe o PostgreSQL e a API juntos; o build do backend acontece dentro da imagem
(não precisa de Java no host):

```bash
docker compose up --build -d
```

- API em `http://localhost:8080` — confirme com
  `curl http://localhost:8080/actuator/health` (deve responder `{"status":"UP"...}`)
- PostgreSQL exposto na porta `5433` do host (5432 dentro da rede do Docker)
- O serviço `api` só inicia após o PostgreSQL passar no *health check*; as
  migrations do Flyway (`V1..V8`) são aplicadas automaticamente no startup

Comandos úteis:

```bash
docker compose logs -f api    # acompanhar o log da API
docker compose down           # parar (preserva os dados)
docker compose down -v        # parar e apagar o volume do banco (recomeço limpo)
```

### Opção B — backend sem Docker

Requer JDK 21 e um PostgreSQL local. Prepare o banco uma vez (os nomes devem
bater com o seu `.env`):

```sql
-- psql como superusuário:
CREATE USER documentos_app WITH PASSWORD 'a_mesma_senha_do_env';
CREATE DATABASE documentos_academicos OWNER documentos_app;
```

Confira no `.env` que `DB_URL` aponta para o seu PostgreSQL local
(`jdbc:postgresql://localhost:5432/documentos_academicos`). Atenção: **o Spring
Boot não lê o arquivo `.env` sozinho** (quem faz isso é o Docker Compose) — ao
rodar sem Docker, exporte as variáveis no shell antes de subir. No Git
Bash/Linux/macOS:

```bash
set -a; source .env; set +a
./mvnw spring-boot:run
```

No PowerShell:

```powershell
Get-Content .env | Where-Object { $_ -match '^[^#].*=' } | ForEach-Object {
  $par = $_ -split '=', 2; Set-Item -Path "Env:$($par[0])" -Value $par[1]
}
.\mvnw.cmd spring-boot:run
```

O Flyway aplica as migrations no startup — não execute SQL de schema na mão.

### Frontend (igual nos dois modos)

```bash
cd frontend
npm install     # primeira vez
npm run dev     # dev server em http://localhost:3000
```

Abra `http://localhost:3000` e entre com o `ADMIN_LOGIN`/`ADMIN_PASSWORD`
definidos no `.env`. Para servir uma versão otimizada:

```bash
npm run build    # gera frontend/dist (tsc + vite build)
npm run preview  # serve o build localmente
```

### Primeiro acesso — roteiro sugerido

1. Entre como administrador (criado automaticamente no primeiro startup).
2. Em **Cadastros**, crie ao menos um aluno, um curso e um tipo de documento —
   sem eles não há o que solicitar.
3. Em **Cadastros → Usuários**, crie os demais usuários (`OPERADOR` para quem
   opera solicitações, `CONSULTA` para somente leitura).
4. Em **Solicitações → Nova solicitação**, crie a primeira solicitação, anexe
   documentos na tela de detalhe e movimente o status pelo fluxo.
5. O **Início** mostra os indicadores; **"Gerar PDF"** na listagem emite o
   relatório com os filtros aplicados.

---

## Testes

A suíte tem dois níveis, separados por fase do Maven:

```bash
./mvnw test      # 104 testes unitários (rápidos, sem Docker)
./mvnw verify    # os 104 + 16 de integração (PostgreSQL real) + análise estática
```

| Nível | Onde | Como |
|---|---|---|
| Unitário | `src/test/.../service`, `mapper`, `specification` | Mockito puro; regras de negócio **sem banco** |
| Fatia web | `...ControllerTest` | `@WebMvcTest` + `@MockitoBean` |
| Consulta | `...QueriesTest`, `...RepositoryTest` | `@DataJpaTest` + H2 |
| Integração | `src/test/.../integracao/*IT` | `@SpringBootTest` + Testcontainers (PostgreSQL 17) |

O relatório PDF também é testado de verdade: o `RelatorioServiceImplTest`
compila o JRXML real do classpath, preenche com dados e valida a assinatura
`%PDF-` do arquivo gerado — não é mock do Jasper.

Para o frontend, a checagem de tipos roda junto do build:

```bash
cd frontend
npm run build   # tsc -b + vite build — falha em erro de tipo
npm run lint    # oxlint
```

### Testes de integração (Testcontainers)

Sobem um **PostgreSQL 17 real**, na mesma versão da produção. O perfil `it` liga o
Flyway e mantém `ddl-auto=validate` — cada execução prova que as migrations
`V1..V8` constroem um schema compatível com as entidades. **Exigem Docker no ar.**

> **`*IT` roda no `verify`, nunca no `package` — e isso é proposital.** O
> `Dockerfile` executa `mvnw clean package` dentro de um container **sem acesso ao
> daemon do Docker**; se os testes de integração rodassem nessa fase, o
> Testcontainers não teria onde subir o banco e o build da imagem quebraria.

### Detalhes não óbvios da suíte

- **A auditoria não pode ser testada com `@DataJpaTest`.** Esse slice roda numa
  transação que sofre *rollback*, e o Envers só grava as linhas de auditoria ao
  encerrar a transação — o `AuditReader` devolveria zero revisões e daria a falsa
  impressão de que o Envers está quebrado. Por isso o `AuditoriaEnversTest` usa
  `TransactionTemplate`, commitando de verdade.
- **O H2 de teste não roda em `MODE=PostgreSQL`.** Nesse modo ele rejeita
  `TINYINT`, o tipo que o Hibernate emite para a coluna `revtype` do Envers, e as
  tabelas de auditoria falhavam silenciosamente na criação.
- **O N+1 é verificado por contagem de consultas**, não por inspeção visual. O
  `ConsultasNMaisUmIT` lê o `Statistics` do Hibernate e falha se alguém remover os
  `@EntityGraph` do `SolicitacaoRepository` (a listagem passaria de ~3 para ~22
  consultas por página).
- **Toda operação da API precisa ter descrição.** O `OpenApiDocsTest` varre o
  documento OpenAPI e falha listando os endpoints sem `summary`.

> **Atenção:** o H2 é mais permissivo que o PostgreSQL. Já houve um bug que passou
> por toda a suíte verde e só apareceu no banco real (nulos sem tipo enviados como
> `bytea`). Os testes de integração existem por causa dele — mas eles cobrem o
> fluxo principal, não tudo: validar no Docker antes de abrir PR continua valendo.

---

## Análise estática

O **SpotBugs** roda no `verify` e falha o build em achados de prioridade alta ou
média:

```bash
./mvnw verify          # inclui a análise
./mvnw spotbugs:check  # só a análise
```

As exclusões ficam em [`spotbugs-exclude.xml`](spotbugs-exclude.xml), e **cada uma
tem justificativa escrita** — um filtro sem motivo é ruído acumulado, e o objetivo
da análise é não ter alerta ignorado sem que alguém tenha decidido ignorá-lo.

Assim como os testes de integração, a análise fica fora do `package` para não
atrasar o build da imagem Docker.

---

## Health check

Com a aplicação no ar, o estado de saúde é exposto pelo Actuator:

```
GET http://localhost:8080/actuator/health
```

Em `dev`, o endpoint mostra detalhes (`show-details=always`); em `prod`, apenas
o status geral.

---

## Limitações conhecidas e decisões técnicas

Consolidação do que foi decidido conscientemente e do que ficou de fora — cada
item existe para que ninguém redescubra esses pontos da forma difícil.

### Decisões técnicas

| Decisão | Motivo |
|---|---|
| Flyway é o único dono do schema (`ddl-auto=validate` em todos os perfis com PostgreSQL) | Schema versionado e reproduzível; o Hibernate só valida. O perfil `it` prova a compatibilidade por teste |
| Testes `*IT` e SpotBugs rodam no `verify`, nunca no `package` | O `Dockerfile` executa `mvnw clean package` **sem acesso ao daemon Docker** — Testcontainers ali quebraria o build da imagem |
| JasperReports **6.21.5**, não 7.x | O 7.x rejeita o formato JRXML clássico ("Unable to load report" sem causa útil); a linha 6.21 lê o formato clássico e exporta PDF via OpenPDF, com tudo no Maven Central |
| `jackson-dataformat-xml` **excluído** da dependência do Jasper | Com esse jar no classpath o Spring MVC registra conversor XML e a API inteira passa a responder XML em `Accept: */*` — as respostas de erro deixariam de ser JSON |
| Imagem de runtime instala `fontconfig` + `fonts-dejavu-core` | O Jasper mede texto via AWT; sem fontes TTF a geração de PDF falha em imagens JRE enxutas |
| Anexos em `bytea` no banco (limite 10MB por arquivo) | Volume esperado baixo; simplifica backup e evita filesystem compartilhado entre containers. Crescendo, o caminho é storage de objetos |
| Conteúdo de anexo fora da listagem (projeção só de metadados) | Listar anexos não deve carregar os bytes de todos os arquivos na memória |
| Usuário não tem `DELETE`; aluno com solicitações não pode ser excluído | Integridade do histórico e da auditoria — inativação no lugar de remoção |
| Ordenação por prioridade via `CASE` na Specification | A prioridade é texto no banco; ordenar a coluna daria ordem alfabética (`ALTA` antes de `URGENTE`) |
| Frontend não re-implementa regras de negócio | A tela esconde/desabilita por conveniência, mas quem valida é sempre o backend (perfil, transição, responsável) — provado por testes que chamam a API por fora da UI |
| Senha e `version` fora da auditoria; exclusões preservam dados | Hash de senha em `_aud` seria passivo de segurança; `store_data_at_delete` evita linhas de DELETE só com id e nulos |
| Swagger desabilitado no perfil `prod` | Console interativo é superfície de ataque desnecessária em produção |

### Limitações assumidas

- **Recuperação de senha sem e-mail:** o código de 6 dígitos volta na resposta
  da API (e aparece na tela) porque não há serviço de e-mail no projeto. Hash,
  validade de 15 minutos e uso único já são definitivos; só o canal de entrega é
  simulado — e isso permite enumerar logins existentes, o que a versão com
  e-mail eliminaria.
- **`responsavel` do status não é validado contra usuário ativo:** um código
  órfão (sem usuário correspondente) trava a movimentação para aquele status até
  ser corrigido via `PUT /api/status/{id}`.
- **Auditoria não é retroativa** e não tem endpoint HTTP: registros anteriores à
  `V6` não têm revisões, e a consulta é via SQL/`AuditReader` (expor esse rastro
  é decisão de produto com implicações próprias de acesso).
- **Sem CI configurada** — a issue de CI foi excluída do escopo por decisão do
  projeto; `./mvnw verify` cobre localmente o que o pipeline rodaria.
- **Uma solicitação por vez:** não há criação em lote nem importação.
- **H2 nos testes unitários é mais permissivo que o PostgreSQL** — por isso os
  testes de integração existem; validar no Docker antes de PR continua valendo.

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

**Milestone 5 — Auditoria e documentação**
- [x] Auditoria das entidades com Hibernate Envers (usuário e data por revisão)
- [x] Documentação interativa com Springdoc / Swagger UI (JWT configurado)

**Milestone 6 — Qualidade**
- [x] Testes unitários das regras críticas, independentes de banco
- [x] Testes de integração com PostgreSQL real (Testcontainers + migrations)
- [x] Refatoração: responsabilidades, N+1 corrigido e medido, análise estática

**Milestone 7 — Frontend**
- [x] Inicialização da SPA (React + TypeScript + Vite)
- [x] Autenticação pela interface (login, sessão, rotas protegidas, logout)
- [x] Gestão de solicitações (formulário, tabela paginada, filtros, status, histórico)
- [x] Indicadores do sistema (cards, gráficos, filtro de período)
- [x] Telas de cadastros (alunos, cursos, tipos, status e usuários)

**Milestone 8 — Extras e entrega**
- [x] CRUD de usuários via API + anexos de documentos nas solicitações
- [x] Recuperação de senha, tema claro/escuro, listagem por prioridade
- [x] Relatórios PDF (JasperReports) com filtros, na API e na interface
- [x] Documentação completa de instalação e execução (este README)

**Fora do escopo / próximos passos**
- [ ] CI (excluída do escopo por decisão do projeto)
- [ ] Validar `responsavel` do status contra usuário ativo (lacuna conhecida)
- [ ] Envio do código de recuperação de senha por e-mail

---

## Licença

Distribuído sob os termos do arquivo [LICENSE](LICENSE).
