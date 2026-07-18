# Deploy em VPS Ubuntu (Hostinger) com domínio e HTTPS

Guia passo a passo para publicar o sistema numa VPS Ubuntu e deixá-lo acessível
pela internet, no seu domínio, com HTTPS automático.

## Arquitetura

Um único domínio, com o **Caddy** na frente fazendo tudo o que é exposto à internet:

```
                    Internet (HTTPS 443)
                          │
                    ┌─────▼─────┐
                    │   Caddy   │  (container "web") — TLS automático (Let's Encrypt)
                    │           │
      site (SPA) ◄──┤  /*       │
                    │  /api/*  ─┼──► ┌─────────┐        ┌────────────┐
                    └───────────┘    │   API   │───────►│ PostgreSQL │
                                     │ :8080   │        │  :5432     │
                                     └─────────┘        └────────────┘
                                      (rede interna do Docker, sem porta pública)
```

- **web (Caddy):** serve o frontend (arquivos estáticos) e encaminha `/api/*` para a API. Único que expõe portas (80/443).
- **api (Spring Boot):** roda no perfil `prod` (Swagger desligado, `ddl-auto=validate`, Flyway aplica as migrations). Não exposta diretamente.
- **postgres:** dados em volume persistente. Não exposto diretamente.

> Como o site e a API ficam no **mesmo domínio**, não há CORS entre navegador e API,
> e o certificado HTTPS cobre tudo.

Todos os arquivos usados aqui já estão no repositório:
[`docker-compose.prod.yml`](../docker-compose.prod.yml), [`.env.prod.example`](../.env.prod.example),
[`frontend/Dockerfile`](../frontend/Dockerfile) e [`frontend/Caddyfile`](../frontend/Caddyfile).

---

## Pré-requisitos

- VPS Ubuntu (22.04 ou 24.04) com acesso `root` ou usuário `sudo` (você já tem, da Hostinger).
- Um domínio (o liberado pela Hostinger serve).
- O IP público da VPS (aparece no hPanel da Hostinger, em **VPS → Visão geral**).

---

## Passo 1 — Apontar o domínio para a VPS (DNS)

No painel onde o domínio é gerenciado (hPanel da Hostinger → **Domínios → Zona DNS**),
crie/edite dois registros do tipo **A** apontando para o IP da VPS:

| Tipo | Nome  | Valor (aponta para) | TTL   |
|------|-------|---------------------|-------|
| A    | `@`   | `IP_DA_SUA_VPS`     | 3600  |
| A    | `www` | `IP_DA_SUA_VPS`     | 3600  |

> A propagação pode levar de alguns minutos a algumas horas. Confira de outra máquina com:
> ```bash
> nslookup seudominio.com
> ```
> Só siga para gerar o HTTPS (Passo 6) depois que o domínio já resolver para o IP da VPS —
> o Let's Encrypt valida o domínio pela internet e falha se o DNS ainda não apontar para cá.

---

## Passo 2 — Acessar a VPS por SSH

Do seu computador (PowerShell no Windows serve):

```bash
ssh root@IP_DA_SUA_VPS
```

(Use a senha/chave definida na Hostinger.)

---

## Passo 3 — Preparar o servidor

```bash
# atualizar o sistema
sudo apt update && sudo apt upgrade -y

# instalar utilitários básicos
sudo apt install -y git ufw

# firewall: liberar SSH, HTTP e HTTPS e ativar
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
sudo ufw status
```

> **Firewall da Hostinger:** além do `ufw`, a Hostinger pode ter um firewall próprio no
> hPanel. Verifique em **VPS → Firewall** e garanta que as portas **80** e **443** (e **22**
> para SSH) estejam liberadas lá também. Se as duas camadas não liberarem, o site não abre.

### (Opcional, mas recomendado em VPS pequena de 1 GB) criar swap

O build da API (Maven) e do frontend (Vite) consome memória. Em VPS com 1 GB de RAM,
crie um arquivo de swap para o build não ser morto por falta de memória:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

---

## Passo 4 — Instalar o Docker

```bash
# instalador oficial (já traz o docker compose plugin)
curl -fsSL https://get.docker.com | sh

# rodar docker sem sudo (opcional): adicione seu usuário ao grupo docker
sudo usermod -aG docker $USER
# saia e entre de novo no SSH para o grupo valer (ou rode: newgrp docker)

# conferir
docker --version
docker compose version
```

---

## Passo 5 — Clonar o projeto e configurar as variáveis

```bash
git clone https://github.com/SamuelDeMarco-Dev/desafio-2026-2-java.git
cd desafio-2026-2-java

# criar o .env de produção a partir do exemplo
cp .env.prod.example .env

# gerar um segredo forte para o JWT e copiar o valor:
openssl rand -base64 48

# editar o .env e preencher tudo
nano .env
```

No `.env`, preencha com cuidado:

| Variável | O que colocar |
|----------|---------------|
| `DOMAIN` | seu domínio, **sem** `https://` — ex.: `seudominio.com` |
| `PUBLIC_URL` | `https://seudominio.com` (mesmo domínio, com https) |
| `ACME_EMAIL` | seu e-mail (avisos de expiração do certificado) |
| `DB_PASSWORD` | uma senha forte para o banco |
| `ADMIN_PASSWORD` | a senha do primeiro administrador (você usará para logar) |
| `JWT_SECRET` | cole o valor gerado pelo `openssl rand -base64 48` |

> Salve no `nano` com `Ctrl+O`, `Enter`, e saia com `Ctrl+X`.

---

## Passo 6 — Subir a aplicação

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

O primeiro build baixa dependências e compila backend e frontend — pode levar
**vários minutos**. Ao final, o Caddy detecta o domínio e emite o certificado HTTPS
automaticamente (só funciona se o DNS do Passo 1 já apontar para esta VPS).

Confira o estado e os logs:

```bash
# containers rodando
docker compose -f docker-compose.prod.yml ps

# logs em tempo real (Ctrl+C para sair)
docker compose -f docker-compose.prod.yml logs -f

# logs só do Caddy (para acompanhar a emissão do certificado)
docker compose -f docker-compose.prod.yml logs -f web

# saúde da API pela rede interna (não está exposta na internet)
docker compose -f docker-compose.prod.yml exec api wget -qO- http://localhost:8080/actuator/health
```

---

## Passo 7 — Primeiro acesso

Abra no navegador:

```
https://seudominio.com
```

Entre com o `ADMIN_LOGIN` (padrão `administrador`) e o `ADMIN_PASSWORD` definidos no `.env`.
Depois siga o roteiro de primeiro acesso do [`README.md`](../README.md#primeiro-acesso--roteiro-sugerido):
cadastre um aluno, um curso e um tipo de documento, e crie a primeira solicitação.

> **Swagger:** fica **desligado no perfil `prod`** por segurança, então não estará
> disponível no ambiente publicado — apenas em desenvolvimento local.

---

## Atualizar o sistema depois (novas versões)

```bash
cd desafio-2026-2-java
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

O Flyway aplica automaticamente novas migrations ao subir. Os dados do banco ficam
preservados no volume `postgres_data`.

## Parar / reiniciar

```bash
docker compose -f docker-compose.prod.yml stop      # para sem apagar nada
docker compose -f docker-compose.prod.yml up -d     # sobe de novo
docker compose -f docker-compose.prod.yml down      # remove containers (mantém volumes/dados)
```

> **Nunca** use `down -v` em produção sem querer: o `-v` apaga os volumes, inclusive o banco.

---

## Backup do banco de dados

```bash
# gerar um dump (na VPS)
docker compose -f docker-compose.prod.yml exec -T postgres \
  pg_dump -U documentos_app documentos_academicos > backup-$(date +%F).sql

# restaurar um dump
cat backup-2026-07-18.sql | docker compose -f docker-compose.prod.yml exec -T postgres \
  psql -U documentos_app -d documentos_academicos
```

Guarde os dumps fora da VPS (baixe com `scp` para sua máquina).

---

## Solução de problemas

| Sintoma | Causa provável / o que checar |
|---------|-------------------------------|
| Navegador não abre / demora e cai | DNS ainda não propagou, ou portas 80/443 fechadas no `ufw` **ou** no firewall do hPanel. |
| "Not secure" / erro de certificado | O Caddy não conseguiu emitir o TLS. Veja `logs -f web`. Quase sempre é DNS não apontando para a VPS ou a porta 80 fechada (o desafio ACME usa a 80). |
| Página abre, mas login dá erro de rede | `PUBLIC_URL` no `.env` diferente do domínio real. Corrija e rode `up -d --build` de novo (a URL da API é embutida no build do frontend). |
| API responde 5xx logo após subir | O banco ainda estava iniciando. O compose já espera o `healthcheck`, mas veja `logs -f api`. |
| Build morto / "Killed" durante o `up` | Falta de memória. Crie o swap (Passo 3). |
| Esqueci a senha do admin | Recuperação de senha existe na tela de login, mas **não há envio de e-mail** neste projeto: o código de recuperação volta na resposta da API (limitação documentada no README). Alternativa: ajuste direto no banco via `psql`. |

---

## Notas de segurança

- Segredos ficam só no `.env` da VPS (fora do Git). Se algum vazar, gere outro e rode `up -d --build`.
- Banco e API **não** têm porta pública — só o Caddy (80/443) é acessível de fora.
- O perfil `prod` desliga o Swagger e não mostra detalhes internos no health check.
- Mantenha o sistema atualizado (`apt upgrade`) e considere `fail2ban` para proteger o SSH.