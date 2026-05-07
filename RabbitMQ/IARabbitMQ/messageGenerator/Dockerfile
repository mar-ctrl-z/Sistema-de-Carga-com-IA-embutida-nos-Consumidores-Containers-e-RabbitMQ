FROM eclipse-temurin:17-jdk

WORKDIR /app

# instala curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# baixa cliente RabbitMQ
RUN curl -L -o /app/rabbitmq-client.jar https://repo1.maven.org/maven2/com/rabbitmq/amqp-client/5.20.0/amqp-client-5.20.0.jar

# baixa SLF4J simples
RUN curl -L -o /app/slf4j-api-2.0.7.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar && \
    curl -L -o /app/slf4j-simple-2.0.7.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.7/slf4j-simple-2.0.7.jar

# cria pastas de saída e datasets
RUN mkdir -p /app/out /app/Dataset_transit /app/Dataset_plates

# copia o código (sem montar volume)
COPY messageGenerator/src /app/src
COPY Dataset_transit /app/Dataset_transit/
COPY Dataset_plates /app/Dataset_plates/

CMD ["sh", "-c", "javac -cp '/app/rabbitmq-client.jar:/app/slf4j-api-2.0.7.jar:/app/slf4j-simple-2.0.7.jar' /app/src/main/java/com/example/MessageGenerator.java -d /app/out && java -cp '/app/out:/app/rabbitmq-client.jar:/app/slf4j-api-2.0.7.jar:/app/slf4j-simple-2.0.7.jar' com.example.MessageGenerator"]

