# Despliegue — Banco de Anteojos

Producción en **DonWeb Cloud Server** (VPS Ubuntu 24.04, 2 vCPU, 2 GB RAM + 2 GB swap) con
**Docker Compose + Caddy**. El servidor ya está preparado con `deploy/setup-server.sh` (solo
documentación: no hay que volver a correrlo).

## Arquitectura

`docker-compose.prod.yml` levanta 3 servicios en `/opt/banco-anteojos`:

| Servicio | Imagen | Puertos publicados | Memoria |
|---|---|---|---|
| `db` | `postgres:16-alpine` | ninguno (Docker se saltea UFW: **nunca** publicar 5432) | 512m |
| `api` | `backend/Dockerfile` (Spring Boot, perfil `prod`) | ninguno | 1g |
| `web` | `frontend/Dockerfile` (build de React + Caddy) | 80, 443/tcp, 443/udp | 256m |

URLs:
- `https://${DOMAIN}` → frontend React (Caddy sirve `dist/`, con fallback SPA a `index.html`).
- `https://api.${DOMAIN}` → API (Caddy hace reverse proxy a `api:8080`). Actuator responde 404.
- Webhook de 17TRACK: **`POST https://api.${DOMAIN}/v1/shipments/webhook`** (público; se autentica
  por la firma del push con `TRACK17_API_KEY`).

Caddy obtiene y renueva solo los certificados de Let's Encrypt. El volumen `caddy_data` guarda los
certificados: **no borrarlo** (Let's Encrypt limita las emisiones).

## Variables de entorno (`/opt/banco-anteojos/.env`)

Plantilla: `.env.prod.example`. El contenedor `api` recibe **solo** las variables listadas en su
bloque `environment:` del compose: agregar una variable al `.env` no alcanza para que llegue a Spring.

| Variable | Obligatoria | Notas |
|---|---|---|
| `DOMAIN` | sí | Hoy `201-32-131-186.sslip.io`. |
| `ACME_EMAIL` | recomendada | Avisos de Let's Encrypt. |
| `DB_PASSWORD` | sí | `openssl rand -base64 32` |
| `JWT_SECRET` | sí | Clave HMAC: **al menos 32 bytes**. `openssl rand -base64 32` |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | sí (primer arranque) | Admin inicial: se crea si no existe ningún ADMIN. La app todavía no permite cambiar contraseñas, así que elegí una definitiva. |
| `R2_*` | sí | Bucket de **producción**, privado (se sirve con URLs firmadas), token restringido a ese bucket. |
| `TRACK17_API_KEY` | para logística | También firma los webhooks entrantes. |
| `MAIL_*` | no | SMTP de notificaciones de turnos (RF-22). `MAIL_HOST` vacío = desactivadas. |

## Acceso

```bash
# ~/.ssh/config en la PC del desarrollador
Host banco
    HostName 201.32.131.186
    Port 5151
    User deploy
    IdentityFile ~/.ssh/id_ed25519
```

Cada integrante usa **su propia clave**: se agrega su `.pub` como una línea más en
`/home/deploy/.ssh/authorized_keys`. Respaldo de acceso: consola web del panel DonWeb (usuario
`deploy` con su contraseña).

## Primer despliegue

```bash
ssh banco
# Deploy key de solo lectura para GitHub
ssh-keygen -t ed25519 -C "deploy-banco" -f ~/.ssh/github_deploy -N ""
cat ~/.ssh/github_deploy.pub     # → GitHub: repo → Settings → Deploy keys (sin write access)
printf "Host github.com\n  IdentityFile ~/.ssh/github_deploy\n" >> ~/.ssh/config

git clone git@github.com:ivanochoa229/banco_de_anteojos.git /opt/banco-anteojos
cd /opt/banco-anteojos
cp .env.prod.example .env && chmod 600 .env
openssl rand -base64 32          # x2: DB_PASSWORD y JWT_SECRET
nano .env

docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml logs -f api   # Flyway + "Started ..."
```

Probar: `https://<DOMAIN>` → login con el admin inicial.
Después: registrar en 17TRACK la URL del webhook (`https://api.<DOMAIN>/v1/shipments/webhook`).

## Alta de operadores (DBA)

La base arranca sin operadores (la migración inicial no siembra usuarios). Los da de alta el DBA
directo en Postgres. `password_hash` tiene que ser un hash **BCrypt**, nunca la contraseña en claro:

```bash
# Generar el hash sin instalar nada (Spring acepta el prefijo $2y$ que produce htpasswd).
# Ojo: la contraseña queda en el historial del shell; borrala después con `history -d`.
docker run --rm httpd:2-alpine htpasswd -bnBC 10 "" 'LaContraseña' | tr -d ':\n'; echo

docker compose -f docker-compose.prod.yml exec db psql -U banco_app -d banco_anteojos
```

```sql
-- Email en minúsculas: el login lo normaliza antes de buscarlo.
INSERT INTO users (name, email, password_hash, role, is_active, created_at)
VALUES ('Nombre Apellido', 'persona@fundacion.org', '<hash>', 'OPERATOR', true, now());

-- Dar de baja (no se borra: las acciones quedan asociadas al usuario).
UPDATE users SET is_active = false WHERE email = 'persona@fundacion.org';
```

## Actualizar a una versión nueva

```bash
ssh banco
cd /opt/banco-anteojos
git pull
docker compose -f docker-compose.prod.yml up -d --build
docker image prune -f
```

## Cambiar al dominio definitivo

1. Registros DNS: `A @ → 201.32.131.186` y `A api → 201.32.131.186`.
2. Editar `DOMAIN` en `/opt/banco-anteojos/.env`.
3. `docker compose -f docker-compose.prod.yml up -d --build` (rebuild necesario: `VITE_API_URL` se
   inyecta en el build del frontend, y `FRONTEND_URL` —el origen CORS del backend— sale de `DOMAIN`).
4. Actualizar la URL del webhook en 17TRACK y el CORS del bucket R2.

## Validación local (antes de subir cambios de deploy)

```bash
# Con un .env de prueba (copiado de .env.prod.example, valores dummy):
docker compose -f docker-compose.prod.yml config > /dev/null && echo "compose OK"
docker compose -f docker-compose.prod.yml build
docker run --rm -v "$PWD/Caddyfile:/etc/caddy/Caddyfile:ro" \
  -e DOMAIN=example.test -e ACME_EMAIL=test@example.test \
  caddy:2-alpine caddy validate --config /etc/caddy/Caddyfile
```

Prueba con memoria limitada (512 MB para la API, sin swap):

```bash
cp .env.local.example .env.local
docker compose -f docker-compose.local.yml --env-file .env.local up --build
docker stats banco-api banco-db
```

## Comandos útiles

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f --tail=100 api
docker stats
sudo ufw status
```

## Notas técnicas

- Las subidas (PDF de ANSES, recetas, fotos) se procesan en memoria (`MultipartFile.getBytes()`),
  con límite de 15 MB en Spring y en Caddy. Con el tráfico esperado y 512 MB de heap no es un
  problema; si crece la concurrencia de subidas, pasar a streams.

## Pendientes

- Backup diario de Postgres cifrado → bucket R2 separado (script + cron, por hacer).
- Dominio definitivo.
- Bucket R2 de producción con token restringido y CORS con el dominio del frontend.
