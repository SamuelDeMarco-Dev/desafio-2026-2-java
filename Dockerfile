# Etapa 1: compilar a aplicação
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src/ src/

RUN ./mvnw -B clean package


# Etapa 2: executar a aplicação
FROM eclipse-temurin:21-jre

# JasperReports usa AWT para medir texto: sem fontconfig + uma fonte TTF
# instaladas, a geração de PDF falha em imagens JRE enxutas.
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]