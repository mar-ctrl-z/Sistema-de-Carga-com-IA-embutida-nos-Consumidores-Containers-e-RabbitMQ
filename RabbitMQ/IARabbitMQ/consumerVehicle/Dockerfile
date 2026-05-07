# Stage 1: build com Maven
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copia apenas o pom.xml
COPY pom.xml .

# 2. Baixa dependências (cacheável)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B


# 3. Copia o código fonte só depois
COPY src ./src

# 4. Compila e gera o JAR com dependências
RUN mvn clean package assembly:single


# Stage 2: runtime leve
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Instalar Tesseract OCR
RUN apt-get update && apt-get install -y tesseract-ocr tesseract-ocr-eng tesseract-ocr-por && rm -rf /var/lib/apt/lists/*

# Copia o JAR compilado do stage anterior
COPY --from=build /app/target/consumerVehicle-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

CMD ["java", "-jar", "app.jar"]
