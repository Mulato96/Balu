# Stage 1: Build
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

# Instalar Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copiar archivos de configuración de Maven primero (para mejor caching de capas)
COPY pom.xml .
COPY src ./src

# Hacer el build y generar el JAR
RUN mvn clean package -DskipTests -Dcheckstyle.skip=true

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
 && apt-get dist-upgrade -y --no-install-recommends \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar el JAR desde el stage de build
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081
USER 1001:1001

# Configuración optimizada de la JVM
ENV JAVA_OPTS="\
  -Xms4g \
  -Xmx8g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]