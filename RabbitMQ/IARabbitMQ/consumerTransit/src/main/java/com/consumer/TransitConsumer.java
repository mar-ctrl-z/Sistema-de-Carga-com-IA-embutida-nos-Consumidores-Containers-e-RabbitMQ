package com.consumer;

import com.rabbitmq.client.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class TransitConsumer {
    private static final String EXCHANGE_NAME = "images";
    private static final String QUEUE_NAME    = "fila_transito";
    private static final String ROUTING_KEY   = "sign";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = null;

        // Loop de retry até o RabbitMQ estar disponível
        while (connection == null) {
            try {
                connection = factory.newConnection();
            } catch (Exception e) {
                System.out.println("RabbitMQ ainda não disponível, tentando novamente em 3s...");
                Thread.sleep(3000);
            }
        }

        Channel channel = connection.createChannel();

        // CORREÇÃO: Declarar exchange, fila e binding aqui no consumidor também.
        // Sem isso, se o consumidor subir antes do gerador a fila não existe
        // e o basicConsume lança ChannelException, derrubando o container.
        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        TransitModel model = new TransitModel();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String message = new String(delivery.getBody());

                String[] parts = message.split(":::");
                String base64      = parts[0];
                String nomeArquivo = parts[1];

                byte[] imageBytes = Base64.getDecoder().decode(base64);

                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

                // Pré-processar imagem para melhor OCR: converter para grayscale
                BufferedImage grayImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = grayImg.createGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();

                String prediction = model.predict(grayImg);
                System.out.println("[Arquivo] " + nomeArquivo + " | [Placa Transito Detectada] " + prediction);

                Thread.sleep(5000); // processamento mais lento que o gerador (enche a fila)
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (Exception e) {
                System.err.println("Erro ao processar a imagem: " + e.getMessage());
                e.printStackTrace();
            }
        };

        channel.basicQos(1);
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        System.out.println("Consumidor Transit pronto, aguardando mensagens na fila '" + QUEUE_NAME + "'...");
    }
}
