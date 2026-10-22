#!/usr/bin/env bash
# deploy.sh — actualiza producción a un commit de main con las imágenes que compiló
# GitHub Actions (ghcr.io). El servidor no compila nada.
# Lo ejecuta .github/workflows/deploy.yml por SSH (forced command en authorized_keys, ver
# docs/DEPLOY.md) y también se puede correr a mano en el servidor:
#   /opt/banco-anteojos/deploy/deploy.sh [sha]     # sin sha: el último commit de main
# Las imágenes de ese commit tienen que existir: las sube el workflow antes de llamarlo.
#
# Todo va entre { ... } para que bash lea el script completo antes de ejecutarlo: el
# git merge de abajo puede reescribir este mismo archivo mientras corre.
{
set -euo pipefail

APP_DIR=/opt/banco-anteojos
COMPOSE="docker compose -f docker-compose.prod.yml"

# Con forced command el SHA llega en SSH_ORIGINAL_COMMAND; a mano, como argumento.
SHA="${SSH_ORIGINAL_COMMAND:-${1:-}}"

cd "$APP_DIR"

# Un deploy a la vez (el workflow ya los encola, esto cubre los manuales).
exec 9>/tmp/banco-deploy.lock
flock -n 9 || { echo "Ya hay un deploy en curso"; exit 1; }

git fetch --quiet origin main
TIP="$(git rev-parse origin/main)"
SHA="${SHA:-$TIP}"
[[ "$SHA" =~ ^[0-9a-f]{40}$ ]] || { echo "SHA inválido: $SHA"; exit 1; }

# Solo se despliega la punta de main: si ya entró otro merge, su propio deploy lo cubre
# (y así nunca sube un commit que todavía no pasó los tests).
if [[ "$SHA" != "$TIP" ]]; then
  echo "main ya avanzó a $TIP: se saltea $SHA"
  exit 0
fi

# El repo sigue haciendo falta en el servidor: compose, Caddyfile (montado en web) y este script.
echo "==> Código: $(git rev-parse --short HEAD) -> ${SHA:0:7}"
# --ff-only: si alguien editó archivos versionados en el servidor, falla en vez de pisarlos.
git merge --ff-only --quiet "$SHA"

# IMAGE_TAG queda en el .env para que un "docker compose up -d" a mano use la misma versión.
sed -i '/^IMAGE_TAG=/d' .env
echo "IMAGE_TAG=$SHA" >> .env

echo "==> Descargando imágenes ${SHA:0:7}"
$COMPOSE pull api web

echo "==> Recreando contenedores"
$COMPOSE up -d --remove-orphans
# -a: borra también las imágenes de deploys anteriores (las que usa un contenedor quedan).
docker image prune -af

# Sin actuator: cualquier respuesta 2xx-4xx de la API significa que Spring levantó
# (Caddy devuelve 502 mientras api:8080 no atiende).
echo "==> Esperando a la API"
DOMAIN="$(grep -E '^DOMAIN=' .env | cut -d= -f2- | tr -d '"')"
for _ in $(seq 1 36); do
  code="$(curl -s -o /dev/null -w '%{http_code}' "https://api.${DOMAIN}/" || true)"
  if [[ "$code" =~ ^[234] ]]; then
    echo "API OK (HTTP $code). Deploy de ${SHA:0:7} terminado."
    exit 0
  fi
  sleep 5
done

echo "La API no respondió en 3 minutos"
$COMPOSE logs --tail=80 api
exit 1
}
