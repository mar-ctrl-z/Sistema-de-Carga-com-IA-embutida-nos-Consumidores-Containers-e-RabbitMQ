package com.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.File;
import java.util.Random;

import java.nio.file.Files;
import java.util.Base64;

public class MessageGenerator {
    private static final Random random = new Random();
    private static final File[] placasTransito;
    private static final File[] placasVeiculo;

    static {
        placasTransito = new File("/app/Dataset_transit").listFiles();
        placasVeiculo = new File("/app/Dataset_plates").listFiles();
    }

    public static void main(String[] args) throws Exception {
        // Conectar no RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq"); // nome do container RabbitMQ no docker-compose
        factory.setUsername("guest");
        factory.setPassword("guest");

        while(true) {
            try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
                String exchangeName = "images";
                channel.exchangeDeclare(exchangeName, "topic", true);

                // Cria filas
                channel.queueDeclare("fila_transito", true, false, false, null);
                channel.queueDeclare("fila_placaVeiculo", true, false, false, null);

                // Faz o bind da fila com a exchange e routing key
                channel.queueBind("fila_transito", exchangeName, "sign");
                channel.queueBind("fila_placaVeiculo", exchangeName, "plate");

                int messagesPerSecond = 10;
                long delay = 1000 / messagesPerSecond;

                while (true) {
                    boolean tipoTransito = random.nextBoolean();
                    String message = generateMessage(tipoTransito);

                    String routingKey;
                    if (tipoTransito) {
                        routingKey = "sign";  // é placa de trânsito
                    } else {
                        routingKey = "plate";  // é placa de veículo
                    }

                    channel.basicPublish(exchangeName, routingKey, null, message.getBytes("UTF-8"));

                    Thread.sleep(delay);
                }
            } catch (Exception e) {
                System.out.println("RabbitMQ não disponível ainda...");
                Thread.sleep(3000);
            }
        }
    }

    private static String generateMessage(boolean tipoTransito) throws Exception {
        File imagemArquivo;

        if (tipoTransito) {
            imagemArquivo = placasTransito[random.nextInt(placasTransito.length)];
        } else {
            imagemArquivo = placasVeiculo[random.nextInt(placasVeiculo.length)];
        }

        String nomeArquivo = imagemArquivo.getName();
        System.out.println("Imagem: " + nomeArquivo + " | Tipo: " + (tipoTransito ? "Transito" : "Veiculo") + " | Timestamp: " + System.currentTimeMillis());

        byte[] bytes = Files.readAllBytes(imagemArquivo.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);

        return base64 + ":::" + nomeArquivo;
    }
}
