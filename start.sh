#!/bin/sh

# Salir si ocurre cualquier error
set -e

echo "--- Iniciando configuración de red ---"

# 1. Iniciar el demonio de Tailscale en modo 'userspace'
# Esto crea un proxy SOCKS5 en localhost:1055 para salir a tu red
# --tun=userspace-networking: Vital para Render/Heroku que no permiten interfaces de red reales
tailscaled --tun=userspace-networking --socks5-server=localhost:1055 &

# 2. Esperar unos segundos a que el servicio arranque
sleep 5

# 3. Conectar a tu red Tailscale
# Usa la variable de entorno TAILSCALE_AUTHKEY que configurarás en Render
echo "--- Conectando a Tailscale ---"
tailscale up --authkey=${TAILSCALE_AUTHKEY} --hostname=render-app-moonphase

# 4. Iniciar tu aplicación Spring Boot usando el Proxy SOCKS
# Esto obliga a Java a usar el túnel de Tailscale para conexiones salientes (BD, FTP)
echo "--- Iniciando Spring Boot ---"
java -DsocksProxyHost=localhost -DsocksProxyPort=1055 -jar app.jar