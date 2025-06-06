package uk.ac.ed.inf.textProc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Preproc {

    /**
     * Converts an image to a monochrome image
     * 
     * @param image BufferedImage of image
     * @return Monochrome image
     */
    public static BufferedImage convertToMonochrome(BufferedImage image) {
        BufferedImage monochromeImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = monochromeImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        if (computeAverageLuminance(monochromeImage) > 0.5){
            monochromeImage = invertImage(monochromeImage);
        }
        return monochromeImage;
    }

    /**
     * Computes the average luminance of an image
     * 
     * @param image BufferedImage of image
     * @return Average luminance
     */
    public static double computeAverageLuminance(BufferedImage image) {
        long sumLuminance = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        // Sum luminance
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = image.getRGB(x, y);
                int r = 255 - ((rgba >> 16) & 0xff);
                int g = 255 - ((rgba >> 8) & 0xff);
                int b = 255 - (rgba & 0xff);
                long avgLuminance = (r + g + b) / 3;
                sumLuminance += avgLuminance;
            }
        }
        // Normalize to range [0,1] by dividing by 255
        return (sumLuminance / totalPixels) / 255.0;
    }

    /**
     * Inverts an image
     * 
     * @param image BufferedImage of image
     * @return Inverted image
     */
    public static BufferedImage invertImage(BufferedImage image) {
        BufferedImage invertedImage = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgba = image.getRGB(x, y);
                int a = (rgba >> 24) & 0xff;
                int r = 255 - ((rgba >> 16) & 0xff);
                int g = 255 - ((rgba >> 8) & 0xff);
                int b = 255 - (rgba & 0xff);
                int invertedRGBA = (a << 24) | (r << 16) | (g << 8) | b;
                invertedImage.setRGB(x, y, invertedRGBA);
            }
        }
        return invertedImage;
    }

    /**
     * Preprocesses an image for OCR
     * 
     * @param image BufferedImage of image
     * @return Preprocessed image
     */
    public static BufferedImage preprocessForOCR(BufferedImage image) {
        BufferedImage monochromeImage = convertToMonochrome(image);
        if (computeAverageLuminance(monochromeImage) > 0.5){
            monochromeImage = invertImage(monochromeImage);
        }
        return monochromeImage;
    }
}