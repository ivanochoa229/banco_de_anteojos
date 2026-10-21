# Compila React y lo sirve con Caddy en la misma imagen.

# ---------- Etapa 1: build ----------
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build          # Vite → dist/

# ---------- Etapa 2: Caddy ----------
FROM caddy:2-alpine
COPY --from=build /app/dist /srv
