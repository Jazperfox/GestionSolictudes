# --- Etapa 1: Traer Tailscale desde su imagen oficial ---
FROM tailscale/tailscale:stable AS tailscale_source

# --- Etapa 2: Compilación de tu App Java (Build) ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# INSTALAMOS dos2unix para corregir errores de formato de Windows
RUN apk add --no-cache dos2unix

# Copiar archivos de Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Corregimos saltos de línea y damos permisos (CRUCIAL para que no falle el build)
RUN dos2unix mvnw && chmod +x mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Etapa 3: Imagen Final de Ejecución ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 1. Copiamos los binarios de Tailscale
COPY --from=tailscale_source /usr/local/bin/tailscaled /usr/local/bin/tailscaled
COPY --from=tailscale_source /usr/local/bin/tailscale /usr/local/bin/tailscale

# 2. Instalamos iptables, ca-certificates y dos2unix
RUN apk add --no-cache iptables ip6tables ca-certificates dos2unix

# 3. Directorios para Tailscale
RUN mkdir -p /var/run/tailscale /var/cache/tailscale /var/lib/tailscale

# 4. Copiamos el JAR ESPECÍFICO (Evita el error del asterisco si hay múltiples jars)
# Usamos el nombre que genera tu pom.xml: MoonPhase-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/MoonPhase-0.0.1-SNAPSHOT.jar app.jar

# 5. Copiamos el script de arranque y lo corregimos
COPY start.sh .
RUN dos2unix start.sh && chmod +x start.sh

# Comando de inicio
CMD ["./start.sh"]