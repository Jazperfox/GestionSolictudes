#!/bin/sh

set -e

echo "--- Iniciando configuración de red ---"

tailscaled --tun=userspace-networking --socks5-server=localhost:1055 &

sleep 5

echo "--- Conectando a Tailscale ---"
tailscale up --authkey=${TAILSCALE_AUTHKEY} --hostname=render-app-moonphase

echo "--- Iniciando Spring Boot ---"
java -DsocksProxyHost=localhost -DsocksProxyPort=1055 -jar app.jar