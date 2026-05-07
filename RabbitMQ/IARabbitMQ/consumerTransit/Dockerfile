# Stage 1: build com Maven
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copia apenas o pom.xml
COPY pom.xml .

# 2. Baixa dependências (cacheável)
RUN mvn dependency:go-offline -B

# 3. Copia o código fonte só depois
COPY src ./src

# 4. Compila e gera o JAR com dependências
RUN mvn clean package assembly:single -B


# Stage 2: runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app

# CORREÇÃO 1: Instalar Tesseract OCR (estava faltando — existia no consumerVehicle mas não aqui)
RUN apt-get update && apt-get install -y tesseract-ocr tesseract-ocr-eng tesseract-ocr-por && rm -rf /var/lib/apt/lists/*

# Copia o JAR compilado do stage anterior
COPY --from=build /app/target/consumerTransit-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# CORREÇÃO 2: Removido o "COPY src/main/resources/" que quebrava o build
# porque esse diretório não existe no projeto

CMD ["java", "-jar", "app.jar"]
