# Contexto para Claude Code — Despliegue de Banco de Anteojos en DonWeb

> Pegá este archivo en la raíz del repo (o pasáselo a Claude Code) y pedile:
> **"Leé DEPLOY_CONTEXT.md y aplicá las tareas de la sección 4. Antes de modificar código, mostrame el plan."**

---

## 1. Decisión y estado actual

- **Hosting de producción: DonWeb Cloud Server (VPS)**. Reemplaza al plan anterior en Render (Render queda descartado: borrar `render.yaml` si existe).
- Servidor ya preparado (no hay que tocarlo desde el repo):
  - Ubuntu 24.04 con Docker + Compose preinstalados. 2 vCPU, 2 GB RAM, 2 GB swap, 24 GB disco.
  - IP `201.32.131.186`. SSH **solo por puerto 5151**, usuario `deploy` con clave (root y contraseñas deshabilitados).
  - UFW activo: 5151, 80, 443/tcp, 443/udp. Firewall del panel DonWeb con los mismos puertos.
  - Carpeta de la app: `/opt/banco-anteojos` (vacía, dueño `deploy`).
- Dominio: temporalmente `201-32-131-186.sslip.io` (resuelve a la IP; sirve para HTTPS real). Se reemplaza por el dominio definitivo cambiando solo `DOMAIN` en el `.env` del servidor.
- Esquema de URLs:
  - `https://${DOMAIN}` → frontend React (servido por Caddy)
  - `https://api.${DOMAIN}` → API Spring Boot (Caddy hace reverse proxy a `api:8080`)
- Arquitectura en el servidor: `docker-compose.prod.yml` con 3 servicios:
  - `db` — Postgres 16, sin puertos publicados (Docker se saltea UFW: nunca publicar 5432).
  - `api` — imagen del `backend/Dockerfile`, sin puertos publicados.
  - `web` — imagen del `frontend/Dockerfile` (build de React + Caddy), único que publica 80/443 y gestiona HTTPS.
- Stack recordatorio: Java 17, Spring Boot 4.1.0, Maven, Flyway (`ddl-auto: none`), JWT, Cloudflare R2, 17TRACK webhook, React (Vite).

## 2. Estructura objetivo del repo

```
<raíz del repo>/
├── docker-compose.prod.yml      ← producción (DonWeb)
├── docker-compose.local.yml     ← prueba local con límites de memoria
├── Caddyfile
├── .env.prod.example
├── .env.local.example
├── .gitignore                   ← debe incluir .env y .env.local
├── deploy/
│   └── setup-server.sh          ← documentación: ya se ejecutó en el servidor
├── docs/
│   └── DEPLOY.md                ← runbook (sección 5 de este archivo)
├── backend/
│   ├── Dockerfile
│   ├── .dockerignore
│   └── src/main/resources/application-prod.yml
└── frontend/
    ├── Dockerfile
    └── .dockerignore
```

## 3. Contenido de los archivos

### 3.1 `docker-compose.prod.yml`

```yaml
# docker-compose.prod.yml — producción en DonWeb
# En el servidor: docker compose -f docker-compose.prod.yml up -d --build
# Variables: se leen del archivo .env en la misma carpeta (ver .env.prod.example)

services:
  db:
    image: postgres:16-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: banco_anteojos
      POSTGRES_USER: banco_app
      POSTGRES_PASSWORD: ${DB_PASSWORD:?falta DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    # SIN "ports": Postgres no se expone fuera de Docker (Docker se saltea UFW)
    mem_limit: 512m
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U banco_app -d banco_anteojos"]
      interval: 10s
      timeout: 5s
      retries: 10

  api:
    build: ./backend
    restart: unless-stopped
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      PORT: 8080
      JAVA_OPTS: "-Xmx512m -Xss512k -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=64m -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: banco_anteojos
      DB_USER: banco_app
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET:?falta JWT_SECRET}
      FRONTEND_URL: https://${DOMAIN}
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      R2_ACCOUNT_ID: ${R2_ACCOUNT_ID}
      R2_ACCESS_KEY_ID: ${R2_ACCESS_KEY_ID}
      R2_SECRET_ACCESS_KEY: ${R2_SECRET_ACCESS_KEY}
      R2_BUCKET: ${R2_BUCKET}
      R2_PUBLIC_URL: ${R2_PUBLIC_URL}
      TRACK17_API_KEY: ${TRACK17_API_KEY}
    # SIN "ports": solo Caddy le habla por la red interna
    mem_limit: 1g

  web:
    build:
      context: ./frontend
      args:
        VITE_API_URL: https://api.${DOMAIN}
    restart: unless-stopped
    depends_on:
      - api
    environment:
      DOMAIN: ${DOMAIN:?falta DOMAIN}
      ACME_EMAIL: ${ACME_EMAIL}
    ports:
      - "80:80"
      - "443:443"
      - "443:443/udp"     # HTTP/3
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy_data:/data       # certificados: NO borrar (Let's Encrypt limita emisiones)
      - caddy_config:/config
    mem_limit: 256m

volumes:
  pgdata:
  caddy_data:
  caddy_config:
```

### 3.2 `Caddyfile`

```caddyfile
# HTTPS automático con Let's Encrypt para ambos dominios.

{
	email {$ACME_EMAIL}
}

# ---------- Frontend (React) ----------
{$DOMAIN} {
	root * /srv
	encode zstd gzip
	try_files {path} /index.html   # SPA: rutas del router sirven index.html
	file_server

	@assets path /assets/*
	header @assets Cache-Control "public, max-age=31536000, immutable"

	header {
		Strict-Transport-Security "max-age=31536000"
		X-Content-Type-Options "nosniff"
		X-Frame-Options "DENY"
		Referrer-Policy "strict-origin-when-cross-origin"
		-Server
	}
}

# ---------- API (Spring Boot) ----------
api.{$DOMAIN} {
	encode zstd gzip

	# Actuator no se expone a Internet
	@actuator path /actuator/*
	respond @actuator 404

	request_body {
		max_size 15MB   # PDFs de ANSES / fotos; debe coincidir con el multipart de Spring
	}

	reverse_proxy api:8080

	header {
		Strict-Transport-Security "max-age=31536000"
		X-Content-Type-Options "nosniff"
		-Server
	}
}
```

### 3.3 `backend/Dockerfile`

```dockerfile
# backend/Dockerfile — build multi-stage (Maven → JRE liviano)

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cachea dependencias: solo se re-descargan si cambia el pom
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario sin privilegios
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app

# Valores por defecto para entornos de 512 MB (docker-compose.local.yml).
# En producción docker-compose.prod.yml los sobreescribe con -Xmx512m.
ENV JAVA_OPTS="-Xmx256m -Xss512k -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=64m -XX:MaxDirectMemorySize=32m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 3.4 `backend/.dockerignore`

```
target/
.idea/
.vscode/
*.iml
.git/
.env
*.log
```

### 3.5 `backend/src/main/resources/application-prod.yml`

> Las claves bajo `app:` son de ejemplo. **Ver tarea 4.3**: hay que alinearlas con los nombres de propiedades que ya usa el código.

```yaml
# Se activa con SPRING_PROFILES_ACTIVE=prod (lo setea docker-compose.prod.yml).

server:
  port: ${PORT:8080}
  forward-headers-strategy: framework   # Caddy termina TLS y reenvía X-Forwarded-*
  tomcat:
    threads:
      max: 40                           # default 200; tráfico bajo, ahorra memoria
      min-spare: 5
  error:
    include-stacktrace: never
    include-message: never

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT:5432}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 5
  jpa:
    hibernate:
      ddl-auto: none                     # el esquema lo maneja Flyway
    open-in-view: false
    show-sql: false
  flyway:
    enabled: true                        # requiere spring-boot-flyway en el pom (Boot 4.1)
    baseline-on-migrate: false
  servlet:
    multipart:
      max-file-size: 15MB                # igual que request_body del Caddyfile
      max-request-size: 15MB

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never

logging:
  level:
    root: INFO
    org.springframework.security: WARN

app:
  jwt:
    secret: ${JWT_SECRET}
  cors:
    allowed-origins: ${FRONTEND_URL}
  admin:
    email: ${ADMIN_EMAIL}
    password: ${ADMIN_PASSWORD}
  r2:
    account-id: ${R2_ACCOUNT_ID}
    access-key-id: ${R2_ACCESS_KEY_ID}
    secret-access-key: ${R2_SECRET_ACCESS_KEY}
    bucket: ${R2_BUCKET}
    public-url: ${R2_PUBLIC_URL}
  track17:
    api-key: ${TRACK17_API_KEY}
```

### 3.6 `frontend/Dockerfile`

```dockerfile
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
```

### 3.7 `frontend/.dockerignore`

```
node_modules
dist
build
.env*
```

### 3.8 `.env.prod.example`

```bash
# En el servidor: copiar como /opt/banco-anteojos/.env  y  chmod 600 .env
# NUNCA subir el .env real a GitHub.

# Dominio (temporal con sslip.io; después el real)
DOMAIN=201-32-131-186.sslip.io
ACME_EMAIL=sistema.gestion@fundacionhacerfuturo.org

# Generar con:  openssl rand -base64 32
DB_PASSWORD=
JWT_SECRET=

# Admin inicial (cambiar la contraseña después del primer login)
ADMIN_EMAIL=
ADMIN_PASSWORD=

# Cloudflare R2 — bucket de PRODUCCIÓN, token con permiso solo a ese bucket
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET=
R2_PUBLIC_URL=

TRACK17_API_KEY=
```

### 3.9 `docker-compose.local.yml` (prueba local con memoria limitada)

```yaml
# Uso: docker compose -f docker-compose.local.yml --env-file .env.local up --build
# Ver consumo: docker stats banco-api banco-db

services:
  db:
    image: postgres:16-alpine
    container_name: banco-db
    environment:
      POSTGRES_DB: banco_anteojos
      POSTGRES_USER: banco_app
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5433:5432"            # 5433 para no chocar con un Postgres local
    volumes:
      - pgdata:/var/lib/postgresql/data
    mem_limit: 256m
    memswap_limit: 256m
    cpus: 0.1
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U banco_app -d banco_anteojos"]
      interval: 5s
      timeout: 3s
      retries: 10

  api:
    build: ./backend
    container_name: banco-api
    depends_on:
      db:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      PORT: 8080
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: banco_anteojos
      DB_USER: banco_app
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      FRONTEND_URL: http://localhost:5173
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      R2_ACCOUNT_ID: ${R2_ACCOUNT_ID}
      R2_ACCESS_KEY_ID: ${R2_ACCESS_KEY_ID}
      R2_SECRET_ACCESS_KEY: ${R2_SECRET_ACCESS_KEY}
      R2_BUCKET: ${R2_BUCKET}
      R2_PUBLIC_URL: ${R2_PUBLIC_URL}
      TRACK17_API_KEY: ${TRACK17_API_KEY}
    mem_limit: 512m
    memswap_limit: 512m        # sin swap: si se pasa, OOMKilled
    cpus: 0.5

volumes:
  pgdata:
```

### 3.10 `.env.local.example`

```bash
# Copiar como .env.local (está en .gitignore)
DB_PASSWORD=local_pass
JWT_SECRET=cambiar_por_un_string_largo_de_al_menos_32_caracteres
ADMIN_EMAIL=admin@test.local
ADMIN_PASSWORD=Admin123!
# Usar el bucket de TEST de R2, nunca el de producción
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET=
R2_PUBLIC_URL=
TRACK17_API_KEY=
```

### 3.11 `deploy/setup-server.sh` (solo documentación — YA fue ejecutado en el servidor)

```bash
#!/usr/bin/env bash
# setup-server.sh — preparación inicial del VPS (Ubuntu 24.04) para Banco de Anteojos
# Uso (como root):  bash setup-server.sh <usuario> "<clave_publica_ssh>" [puerto_ssh]
# Ejemplo:          bash setup-server.sh deploy "ssh-ed25519 AAAA... ivan@pc" 5151
set -euo pipefail

USUARIO="${1:?Falta el nombre de usuario}"
PUBKEY="${2:?Falta la clave pública SSH (entre comillas)}"
SSH_PORT="${3:-22}"

[[ $EUID -eq 0 ]] || { echo "Ejecutalo como root"; exit 1; }
[[ "$PUBKEY" =~ ^ssh-(ed25519|rsa)\  ]] || { echo "La clave pública no parece válida"; exit 1; }

echo "==> 1/7 Actualizando el sistema"
export DEBIAN_FRONTEND=noninteractive
apt-get update -y && apt-get upgrade -y
apt-get install -y ufw fail2ban unattended-upgrades ca-certificates curl git htop

echo "==> 2/7 Zona horaria"
timedatectl set-timezone America/Argentina/Tucuman

echo "==> 3/7 Usuario $USUARIO con sudo y clave SSH"
id "$USUARIO" &>/dev/null || adduser --disabled-password --gecos "" "$USUARIO"
usermod -aG sudo "$USUARIO"
install -d -m 700 -o "$USUARIO" -g "$USUARIO" "/home/$USUARIO/.ssh"
echo "$PUBKEY" > "/home/$USUARIO/.ssh/authorized_keys"
chmod 600 "/home/$USUARIO/.ssh/authorized_keys"
chown "$USUARIO:$USUARIO" "/home/$USUARIO/.ssh/authorized_keys"
if [[ "$(passwd -S "$USUARIO" | awk '{print $2}')" != "P" ]]; then
  echo "Definí una contraseña para $USUARIO (la pide sudo, no SSH):"
  passwd "$USUARIO"
fi

echo "==> 4/7 Endureciendo SSH (sin root, sin contraseñas)"
# 00- para que gane sobre 50-cloud-init.conf (en sshd gana la primera coincidencia)
cat > /etc/ssh/sshd_config.d/00-hardening.conf <<EOF
PermitRootLogin no
PasswordAuthentication no
KbdInteractiveAuthentication no
PubkeyAuthentication yes
MaxAuthTries 3
AllowUsers $USUARIO
EOF
mkdir -p /run/sshd   # con activación por socket no existe y sshd -t falla
sshd -t
if systemctl is-active --quiet ssh; then systemctl reload ssh; fi

echo "==> 5/7 Firewall (UFW) y fail2ban"
ufw default deny incoming
ufw default allow outgoing
ufw allow "${SSH_PORT}/tcp" comment 'SSH'
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 443/udp     # HTTP/3 (Caddy)
ufw --force enable
systemctl enable --now fail2ban

echo "==> 6/7 Actualizaciones de seguridad automáticas + swap de 2 GB"
dpkg-reconfigure -f noninteractive unattended-upgrades
if ! swapon --show | grep -q /swapfile; then
  fallocate -l 2G /swapfile && chmod 600 /swapfile
  mkswap /swapfile && swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
  sysctl -w vm.swappiness=10 && echo 'vm.swappiness=10' > /etc/sysctl.d/99-swap.conf
fi

echo "==> 7/7 Docker + Compose"
if ! command -v docker &>/dev/null; then
  curl -fsSL https://get.docker.com | sh
fi
usermod -aG docker "$USUARIO"
systemctl enable --now docker
if [[ -f /etc/docker/daemon.json ]]; then
  cp /etc/docker/daemon.json /etc/docker/daemon.json.bak
fi
cat > /etc/docker/daemon.json <<EOF
{ "log-driver": "json-file", "log-opts": { "max-size": "10m", "max-file": "3" } }
EOF
systemctl restart docker
install -d -o "$USUARIO" -g "$USUARIO" /opt/banco-anteojos

echo "LISTO. Probá desde otra terminal:  ssh -p $SSH_PORT $USUARIO@<IP>"
```

---

## 4. Tareas para Claude Code

Mostrá el plan antes de modificar código existente. No toques nada fuera de lo que piden estas tareas.

**4.1 Crear/actualizar archivos** según la estructura de la sección 2 y los contenidos de la sección 3. Si alguno ya existe, mostrá el diff antes de reemplazarlo.

**4.2 Limpieza**
- Borrar `render.yaml` si existe.
- `.gitignore` de la raíz: asegurar que incluya `.env` y `.env.local`.
- Verificar que no haya secretos versionados (`git ls-files | grep -i env`, buscar claves hardcodeadas de R2/JWT/17TRACK en el código y en `application*.yml`).

**4.3 Alinear `application-prod.yml` con el código (lo más importante)**
- Buscar dónde el backend lee su configuración (`@Value`, `@ConfigurationProperties`, `application.yml` base) para: secreto JWT, orígenes CORS, admin bootstrap, R2, 17TRACK.
- **Ajustar las claves bajo `app:` del `application-prod.yml` a los nombres que ya usa el código** (preferir cambiar el yml, no el código). Las variables de entorno (`${JWT_SECRET}`, etc.) se mantienen porque las inyecta el compose.
- Revisar que el `application.yml` base no pise nada crítico en el perfil prod (por ejemplo `ddl-auto` distinto de `none`, `show-sql: true`, credenciales por defecto).

**4.4 Verificaciones del backend**
- CORS: debe tomar el origen permitido de la configuración (`FRONTEND_URL` → `https://${DOMAIN}`), nunca `*`. Si está hardcodeado (ej. `localhost:5173`), parametrizarlo.
- `pom.xml`: confirmar que esté el módulo `spring-boot-flyway` (en Boot 4.1, si falta, las migraciones se saltean en silencio).
- Actuator: si `spring-boot-starter-actuator` no está, no hace falta agregarlo (el compose no usa healthcheck HTTP). Si está, que solo exponga `health`.
- Subida de archivos (PDF de ANSES, fotos a R2): confirmar que use streams y no cargue archivos enteros en memoria, y que el límite multipart quede en 15MB (igual que Caddy).
- El webhook de 17TRACK quedará en `https://api.${DOMAIN}/<ruta-del-webhook>`: identificar la ruta exacta e informarla.
- Si el JWT se firma con HMAC, recordar que `JWT_SECRET` debe tener al menos 32 bytes (se genera con `openssl rand -base64 32`).

**4.5 Verificaciones del frontend**
- Confirmar que el proyecto usa **Vite** y que el build sale en `dist/`. Si usa CRA: cambiar `dist` → `build` en `frontend/Dockerfile` y la variable a `REACT_APP_API_URL`.
- Confirmar que **todos** los clientes de API (uno por dominio funcional) usan `import.meta.env.VITE_API_URL` como base, sin `localhost` hardcodeado.
- `VITE_API_URL` apunta a la **raíz** del backend (`https://api.${DOMAIN}`). Verificar cómo se arman las rutas (si los controllers usan prefijo `/api`, el cliente debe agregarlo o la base incluirlo) e informar cómo queda.
- Router SPA: Caddy ya hace fallback a `index.html`; no hace falta nada extra.

**4.6 Validación local (sin desplegar)**
```bash
# Con un .env de prueba (copiado de .env.prod.example, valores dummy):
docker compose -f docker-compose.prod.yml config > /dev/null && echo "compose OK"
docker compose -f docker-compose.prod.yml build
docker run --rm -v "$PWD/Caddyfile:/etc/caddy/Caddyfile:ro" \
  -e DOMAIN=example.test -e ACME_EMAIL=test@example.test \
  caddy:2-alpine caddy validate --config /etc/caddy/Caddyfile
```
Opcional: levantar `docker-compose.local.yml` y medir memoria con `docker stats`.

**4.7 Documentación**
- Crear `docs/DEPLOY.md` con el runbook de la sección 5.
- Actualizar el `CLAUDE.md` raíz (y el de backend si menciona hosting): hosting = **DonWeb Cloud Server con Docker Compose + Caddy**, ya no Render.

**4.8 Commit**
- Un commit con todo (sin `.env` reales), mensaje sugerido: `chore(deploy): configuración de producción para DonWeb (Docker Compose + Caddy)`.
- No hacer push sin confirmación.

---

## 5. Runbook (base para `docs/DEPLOY.md`)

### Acceso
```bash
# ~/.ssh/config en la PC del desarrollador
Host banco
    HostName 201.32.131.186
    Port 5151
    User deploy
    IdentityFile ~/.ssh/id_ed25519
```
Cada integrante usa **su propia clave**: se agrega su `.pub` como una línea más en `/home/deploy/.ssh/authorized_keys`. Respaldo de acceso: consola web del panel DonWeb (usuario `deploy` con su contraseña).

### Primer despliegue
```bash
ssh banco
# Deploy key de solo lectura para GitHub
ssh-keygen -t ed25519 -C "deploy-banco" -f ~/.ssh/github_deploy -N ""
cat ~/.ssh/github_deploy.pub     # → GitHub: repo → Settings → Deploy keys (sin write access)
printf "Host github.com\n  IdentityFile ~/.ssh/github_deploy\n" >> ~/.ssh/config

git clone git@github.com:<USUARIO>/<REPO>.git /opt/banco-anteojos
cd /opt/banco-anteojos
cp .env.prod.example .env && chmod 600 .env
openssl rand -base64 32          # x2: DB_PASSWORD y JWT_SECRET
nano .env

docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml logs -f api   # Flyway + "Started ..."
```
Probar: `https://<DOMAIN>` → login con el admin inicial → cambiar su contraseña.

### Actualizar a una versión nueva
```bash
ssh banco
cd /opt/banco-anteojos
git pull
docker compose -f docker-compose.prod.yml up -d --build
docker image prune -f
```

### Cambiar al dominio definitivo
1. Registros DNS: `A @ → 201.32.131.186` y `A api → 201.32.131.186`.
2. Editar `DOMAIN` en `/opt/banco-anteojos/.env`.
3. `docker compose -f docker-compose.prod.yml up -d --build` (rebuild necesario: `VITE_API_URL` se inyecta en el build del frontend).
4. Actualizar la URL del webhook en 17TRACK y el CORS del bucket R2.

### Comandos útiles
```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f --tail=100 api
docker stats
sudo ufw status
```

### Pendientes
- Backup diario de Postgres cifrado → bucket R2 separado (script + cron, por hacer).
- Dominio definitivo.
- Bucket R2 de producción con token restringido y CORS con el dominio del frontend.
