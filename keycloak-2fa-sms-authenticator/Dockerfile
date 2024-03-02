FROM maven:3.8.6-eclipse-temurin-17-alpine@sha256:e88c1a981319789d0c00cd508af67a9c46524f177ecc66ca37c107d4c371d23b AS builder

WORKDIR /build
COPY . .

RUN --mount=type=cache,target=/root/.m2 mvn clean package -U -DskipTests


FROM quay.io/keycloak/keycloak:20.0.0

COPY gson-2.10.1.jar /opt/keycloak/providers/gson-2.10.1.jar

COPY --from=builder /build/target/*.jar  /opt/keycloak/providers/keycloak-2fa-sms-authenticator-1.0-SNAPSHOT.jar
