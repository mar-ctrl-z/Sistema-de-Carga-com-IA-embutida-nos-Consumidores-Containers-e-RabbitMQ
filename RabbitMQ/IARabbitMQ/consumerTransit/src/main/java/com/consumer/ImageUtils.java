package com.consumer;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;

public class ImageUtils {

    public static double[] imageToVector(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        double[] vector = new double[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = resized.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // considerando imagem em tons de cinza
                vector[y * width + x] = gray / 255.0;
            }
        }
        return vector;
    }

    // Versão que recebe File
    public static double[] imageToVector(File file, int width, int height) throws Exception {
        BufferedImage img = ImageIO.read(file); // lê o arquivo
        return imageToVector(img, width, height); // chama a versão de BufferedImage
    }
}
