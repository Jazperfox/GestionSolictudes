FROM tailscale/tailscale:stable AS tailscale_source

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache dos2unix

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN dos2unix mvnw && chmod +x mvnw


RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=tailscale_source /usr/local/bin/tailscaled /usr/local/bin/tailscaled
COPY --from=tailscale_source /usr/local/bin/tailscale /usr/local/bin/tailscale

RUN apk add --no-cache iptables ip6tables ca-certificates dos2unix

RUN mkdir -p /var/run/tailscale /var/cache/tailscale /var/lib/tailscale

COPY --from=build /app/target/MoonPhase-0.0.1-SNAPSHOT.jar app.jar

COPY start.sh .
RUN dos2unix start.sh && chmod +x start.sh

CMD ["./start.sh"]