# --- Etapa 1: Traer Tailscale desde su imagen oficial ---
# Usamos la imagen oficial de Docker Hub como fuente
FROM tailscale/tailscale:stable AS tailscale_source

# --- Etapa 2: Compilación de tu App Java (Build) ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar archivos de Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Dar permisos de ejecución al wrapper (vital para Linux/Render)
RUN chmod +x mvnw

# Descargar dependencias primero (capa de caché)
RUN ./mvnw dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Etapa 3: Imagen Final de Ejecución ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 1. Copiamos los binarios de Tailscale desde la Etapa 1
COPY --from=tailscale_source /usr/local/bin/tailscaled /usr/local/bin/tailscaled
COPY --from=tailscale_source /usr/local/bin/tailscale /usr/local/bin/tailscale

# 2. Instalamos iptables y ca-certificates (necesarios para VPN)
RUN apk add --no-cache iptables ip6tables ca-certificates

# 3. Crear directorios necesarios para Tailscale
RUN mkdir -p /var/run/tailscale /var/cache/tailscale /var/lib/tailscale

# 4. Copiamos tu aplicación compilada
# El asterisco *.jar toma el nombre generado automáticamente por Maven
COPY --from=build /app/target/*.jar app.jar

# 5. Copiamos el script de arranque
COPY start.sh .
RUN chmod +x start.sh

# Comando de inicio
CMD ["./start.sh"]