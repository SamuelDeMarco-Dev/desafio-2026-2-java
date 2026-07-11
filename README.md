# Sistema de Gestão de Solicitações de Documentos Acadêmicos

API REST para cadastro, consulta e movimentação de solicitações de documentos
acadêmicos (histórico, diploma, atestado de matrícula, etc.).

> **Status:** em desenvolvimento — a infraestrutura base (banco, migrations,
> Docker e perfis de ambiente) já está pronta. As camadas de domínio e a API
> REST ainda estão sendo implementadas. Veja o [Roadmap](#roadmap).

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

```
desafio-2026-2-java/
├── src/
│   ├── main/
│   │   ├── java/br/com/samuel/documentos_academicos/
│   │   │   └── DocumentosAcademicosApplication.java   # bootstrap Spring Boot
│   │   └── resources/
│   │       ├── application.properties                 # config base
│   │       ├── application-dev.properties             # perfil dev
│   │       ├── application-prod.properties            # perfil prod
│   │       └── db/migrations/                         # migrations Flyway
│   │           ├── V1__create_initial_schema.sql
│   │           └── V2__insert_initial_statuses.sql
│   └── test/
│       ├── java/.../DocumentosAcademicosApplicationTests.java
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

- [x] Bootstrap do projeto (Java 21 + Spring Boot + Maven)
- [x] Conexão com PostgreSQL e perfis de ambiente (dev/prod/test)
- [x] Ambiente Docker (API + PostgreSQL via Docker Compose)
- [x] Versionamento do banco com Flyway (schema inicial + status)
- [x] Health check via Actuator
- [ ] Entidades JPA e repositórios
- [ ] Serviços de domínio e regras de negócio do fluxo de solicitação
- [ ] Endpoints REST (CRUD de solicitações e movimentação de status)
- [ ] Validação e tratamento centralizado de erros
- [ ] Autenticação e autorização (Spring Security + JWT)
- [ ] Documentação da API (OpenAPI / Swagger)
- [ ] Testes de integração dos endpoints

---

## Licença

Distribuído sob os termos do arquivo [LICENSE](LICENSE).
