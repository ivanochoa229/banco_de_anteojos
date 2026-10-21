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
