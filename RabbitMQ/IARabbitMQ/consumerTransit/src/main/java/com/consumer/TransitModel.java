package com.consumer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TransitModel {
    private Tesseract tesseract;

    public TransitModel() {
        tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata"); // Ajustar caminho se necessário
        tesseract.setLanguage("eng"); // Inglês para caracteres latinos
    }

    public String predict(BufferedImage imageFile) throws Exception {
        // Pré-processar imagem: converter para grayscale
        BufferedImage img = imageFile;
        BufferedImage grayImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayImg.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        // Usar OCR para extrair texto
        try {
            String extractedText = tesseract.doOCR(grayImg);
            return extractedText.trim();
        } catch (TesseractException e) {
            throw new RuntimeException("Erro ao processar OCR: " + e.getMessage(), e);
        }
    }
}
