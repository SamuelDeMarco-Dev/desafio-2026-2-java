# Etapa 1 — build do SPA (Vite)
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# A URL da API é embutida no bundle em tempo de build. Em produção com domínio
# único, aponte para o próprio domínio: as chamadas usam caminhos /api/... que o
# Caddy encaminha para o container da API.
ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build


# Etapa 2 — Caddy serve o SPA e faz proxy reverso de /api -> api:8080,
# obtendo e renovando o certificado HTTPS automaticamente (Let's Encrypt).
FROM caddy:2-alpine
COPY Caddyfile /etc/caddy/Caddyfile
COPY --from=build /app/dist /srv