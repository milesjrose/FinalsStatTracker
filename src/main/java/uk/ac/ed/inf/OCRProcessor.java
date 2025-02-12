package uk.ac.ed.inf;

// OCRProcessor.java
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.awt.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OCRProcessor {
    public static String extractTextFromImage(String imagePath, Rectangle region) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("eng");
        tesseract.setTessVariable("user_defined_dpi", "300");
        tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,"); // Only recognize numbers & letters
        tesseract.setOcrEngineMode(3);
        tesseract.setPageSegMode(6);    // Treat as a block of text

        try {
            File imageFile = new File(imagePath);
            BufferedImage fullImage = ImageIO.read(imageFile);
            // Crop the specified region
            BufferedImage croppedImage = fullImage.getSubimage(region.x, region.y, region.width, region.height);

            // Convert the cropped image to monochrome
            BufferedImage monoImage = ImagePreprocessor.convertToMonochrome(croppedImage);

            // Save the preprocessed image for debugging purposes
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            timestamp = timestamp + region.x + region.y;
            String preprocessedFileName = "resources/monochrome_" + timestamp + ".png";
            File preprocessedFile = new File(preprocessedFileName);
            preprocessedFile.getParentFile().mkdirs();
            ImageIO.write(monoImage, "png", preprocessedFile);

            // Perform OCR on the preprocessed image
            return tesseract.doOCR(monoImage);
        } catch (TesseractException | java.io.IOException e) {
            e.printStackTrace();
            return "OCR Failed";
        }
    }
}