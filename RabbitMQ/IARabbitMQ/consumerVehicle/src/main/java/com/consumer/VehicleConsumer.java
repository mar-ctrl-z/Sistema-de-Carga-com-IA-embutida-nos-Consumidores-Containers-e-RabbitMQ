package com.consumer;

import com.rabbitmq.client.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class VehicleConsumer {
    private static final String EXCHANGE_NAME = "images";
    private static final String QUEUE_NAME    = "fila_placaVeiculo";
    private static final String ROUTING_KEY   = "plate";

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

        // Inicializar Tesseract
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        tesseract.setLanguage("eng");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String message = new String(delivery.getBody());

                String[] parts = message.split(":::");
                String base64      = parts[0];
                String nomeArquivo = parts[1];

                byte[] imageBytes = Base64.getDecoder().decode(base64);

                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

                // Pré-processar imagem: converter para grayscale
                BufferedImage grayImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = grayImg.createGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();

                // Classificação por nome de arquivo
                String tipoVeiculo = "Carro"; // Padrão
                String nomeLower   = nomeArquivo.toLowerCase();
                if (nomeLower.contains("moto")) {
                    tipoVeiculo = "Moto";
                } else if (nomeLower.contains("caminhao") || nomeLower.contains("truck")) {
                    tipoVeiculo = "Caminhão";
                }

                String extractedText = tesseract.doOCR(grayImg).replaceAll("\n", " ").trim();

                System.out.println("[Arquivo] " + nomeArquivo + " | [Tipo] " + tipoVeiculo + " | [Placa Detectada] " + extractedText);

                Thread.sleep(5000); // processamento mais lento que o gerador (enche a fila)
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            } catch (TesseractException e) {
                System.err.println("Erro ao processar OCR: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Erro ao processar a imagem: " + e.getMessage());
                e.printStackTrace();
            }
        };

        channel.basicQos(1);
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        System.out.println("Consumidor Vehicle pronto, aguardando mensagens na fila '" + QUEUE_NAME + "'...");
    }
}
