package uk.ac.ed.inf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImagePreprocessor {

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
}