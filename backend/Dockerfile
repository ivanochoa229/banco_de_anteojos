# backend/Dockerfile — build multi-stage (Maven → JRE liviano)

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cachea dependencias: solo se re-descargan si cambia el pom
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario sin privilegios
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app

# Valores por defecto para entornos de 512 MB (docker-compose.local.yml).
# En producción docker-compose.prod.yml los sobreescribe con -Xmx512m.
ENV JAVA_OPTS="-Xmx256m -Xss512k -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=64m -XX:MaxDirectMemorySize=32m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
