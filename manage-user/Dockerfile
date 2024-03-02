FROM maven:3.8.6-eclipse-temurin-17-alpine@sha256:e88c1a981319789d0c00cd508af67a9c46524f177ecc66ca37c107d4c371d23b AS builder

WORKDIR /build
COPY . .

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:17.0.5_8-jre-alpine@sha256:02c04793fa49ad5cd193c961403223755f9209a67894622e05438598b32f210e

WORKDIR /opt/app
COPY --from=builder /build/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]