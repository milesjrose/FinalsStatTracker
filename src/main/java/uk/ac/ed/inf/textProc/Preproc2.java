package uk.ac.ed.inf.textProc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ed.inf.utility.FileUtil;

public class Preproc2 {

    public static final boolean DEBUG = true;

    private static final Logger logger = LoggerFactory.getLogger(Preproc2.class);

    /**
     * Scales the image by a given factor.
     * 
     * @param originalImage BufferedImage of image
     * @param scaleFactor Double of scale factor
     * @return Scaled image
     */
    public static BufferedImage scaleImage(BufferedImage originalImage, double scaleFactor) {
        if (scaleFactor == 1.0) return originalImage; // No scaling needed

        int newWidth = (int) (originalImage.getWidth() * scaleFactor);
        int newHeight = (int) (originalImage.getHeight() * scaleFactor);

        // Ensure new dimensions are at least 1x1
        if (newWidth < 1) newWidth = 1;
        if (newHeight < 1) newHeight = 1;

        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight,
                originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType()); // Handle 0 type

        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return scaledImage;
    }

    /**
     * Converts an image to grayscale.
     * 
     * @param originalImage BufferedImage of image
     * @return Grayscale image
     */
    public static BufferedImage toGrayscale(BufferedImage originalImage) {
        if (originalImage.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return originalImage;
        }
        BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        return grayImage;
    }

    /**
     * Inverts the colors of a grayscale image.
     * White becomes black, black becomes white.
     * 
     * @param grayscaleImage BufferedImage of grayscale image
     * @return Inverted grayscale image
     */
    public static BufferedImage invertGrayscaleImage(BufferedImage grayscaleImage) {
        if (DEBUG){
            long tag = System.currentTimeMillis();
            System.out.println("Inverting grayscale image");
            FileUtil.writeImageToFile(grayscaleImage, "preproc/invert_" + tag + ".png");
        }
        // First ensure we have a non-indexed image
        BufferedImage nonIndexedImage;
        if (grayscaleImage.getType() == BufferedImage.TYPE_BYTE_INDEXED || 
            grayscaleImage.getType() == BufferedImage.TYPE_BYTE_BINARY) {
            nonIndexedImage = new BufferedImage(grayscaleImage.getWidth(), grayscaleImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = nonIndexedImage.createGraphics();
            g.drawImage(grayscaleImage, 0, 0, null);
            g.dispose();
        } else {
            nonIndexedImage = grayscaleImage;
        }

        // Create output image with TYPE_INT_ARGB
        BufferedImage invertedImage = new BufferedImage(nonIndexedImage.getWidth(), nonIndexedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
        RescaleOp op = new RescaleOp(-1.0f, 255f, null);
        op.filter(nonIndexedImage, invertedImage);
        return invertedImage;
    }

    /**
     * Calculates the average pixel intensity of a grayscale image.
     * Returns a value between 0 (all black) and 255 (all white).
     * 
     * @param grayscaleImage BufferedImage of grayscale image
     * @return Average pixel intensity
     */
    public static double getAveragePixelIntensity(BufferedImage grayscaleImage) {
        if (grayscaleImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            logger.warn("getAveragePixelIntensity called on non-grayscale image. Result might be unexpected.");
            grayscaleImage = toGrayscale(grayscaleImage);
        }
        long sumIntensity = 0;
        int width = grayscaleImage.getWidth();
        int height = grayscaleImage.getHeight();
        if (width == 0 || height == 0) return 128; // Avoid division by zero for empty image

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sumIntensity += (grayscaleImage.getRGB(x, y) & 0xff);
            }
        }
        return (double) sumIntensity / (width * height);
    }

    /**
     * Binarizes a grayscale image using a fixed threshold.
     * Pixels darker than threshold become black, others become white.
     * 
     * @param grayscaleImage BufferedImage of grayscale image
     * @param threshold Integer of threshold
     * @return Binarized image
     */
    public static BufferedImage binarize(BufferedImage grayscaleImage, int threshold) {
        if (grayscaleImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            logger.warn("binarize called on non-grayscale image. Converting to gray first.");
            grayscaleImage = toGrayscale(grayscaleImage);
        }
        BufferedImage binaryImage = new BufferedImage(grayscaleImage.getWidth(), grayscaleImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < grayscaleImage.getHeight(); y++) {
            for (int x = 0; x < grayscaleImage.getWidth(); x++) { 
                int gray = grayscaleImage.getRGB(x, y) & 0xff;
                if (gray < threshold) {
                    binaryImage.setRGB(x, y, Color.BLACK.getRGB()); // Text
                } else {
                    binaryImage.setRGB(x, y, Color.WHITE.getRGB()); // Background
                }
            }
        }
        return binaryImage;
    }

    /**
     * Binarizes a grayscale image using Otsu's method to automatically find the threshold.
     * This is generally more robust than a fixed threshold.
     * 
     * @param grayscaleImage BufferedImage of grayscale image
     * @return Binarized image
     */
    public static BufferedImage otsuBinarize(BufferedImage grayscaleImage) {
        if (grayscaleImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            logger.warn("otsuBinarize called on non-grayscale image. Converting to gray first.");
            grayscaleImage = toGrayscale(grayscaleImage);
        }
        int threshold = getOtsuThreshold(grayscaleImage);
        return binarize(grayscaleImage, threshold);
    }

    /**
     * Calculates the optimal threshold for binarization using Otsu's method.
     * 
     * @param grayscaleImage BufferedImage of grayscale image
     * @return Threshold
     */
    public static int getOtsuThreshold(BufferedImage grayscaleImage) {
        int[] histogram = new int[256];
        int width = grayscaleImage.getWidth();
        int height = grayscaleImage.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                histogram[grayscaleImage.getRGB(x, y) & 0xff]++;
            }
        }

        int totalPixels = width * height;
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        float sumB = 0;
        int wB = 0; // Weight Background
        int wF; // Weight Foreground

        float varMax = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histogram[t]; // Weight Background
            if (wB == 0) continue;

            wF = totalPixels - wB; // Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histogram[t]);

            float mB = sumB / wB; // Mean Background
            float mF = (sum - sumB) / wF; // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }
        return threshold + 1; // +1 can give better visual results
    }


    /**
     * Adds padding around the image.
     * 
     * @param image BufferedImage of image
     * @param padding Integer of padding
     * @param backgroundColor Color of background
     * @return Padded image
     */
    public static BufferedImage addPadding(BufferedImage image, int padding, Color backgroundColor) {
        if (padding <= 0) return image;

        int newWidth = image.getWidth() + 2 * padding;
        int newHeight = image.getHeight() + 2 * padding;

        BufferedImage paddedImage = new BufferedImage(newWidth, newHeight,
            image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType()); // Handle type 0

        Graphics2D g = paddedImage.createGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, newWidth, newHeight);
        g.drawImage(image, padding, padding, null);
        g.dispose();
        return paddedImage;
    }


    /**
     * Main preprocessing pipeline.
     * Aims to produce a clean, black-text-on-white-background binary image.
     * 
     * @param originalImage BufferedImage of image
     * @param scaleFactor Double of scale factor
     * @param paddingAmount Integer of padding amount
     * @return Preprocessed image
     */

    public static BufferedImage preprocessIntForOCR(BufferedImage originalImage, double scaleFactor, int paddingAmount, boolean autoInvert){
        return preprocessForOCR(originalImage, scaleFactor, paddingAmount, autoInvert, true);
    }

    public static BufferedImage preprocessStrForOCR(BufferedImage originalImage, double scaleFactor, int paddingAmount, boolean autoInvert){
        return preprocessForOCR(originalImage, scaleFactor, paddingAmount, autoInvert, false);
    }

    /**
     * Main preprocessing pipeline.
     * Aims to produce a clean, black-text-on-white-background binary image.
     * 
     * @param originalImage BufferedImage of image
     * @param scaleFactor Double of scale factor
     * @param paddingAmount Integer of padding amount
     * @return Preprocessed image
     */
    public static BufferedImage preprocessForOCR(BufferedImage originalImage, double scaleFactor, int paddingAmount, boolean autoInvert, boolean isInt) {
        if (DEBUG){
            long tag = System.currentTimeMillis();
            FileUtil.writeImageToFile(originalImage, "preproc/original_" + tag + ".png");
        }
        BufferedImage currentImage = originalImage;

        // 1. Scale the image 
        if (scaleFactor > 1.0) {
            currentImage = scaleImage(currentImage, scaleFactor);
        }

        // 2. Convert to Grayscale
        currentImage = toGrayscale(currentImage);

        // 3. Ensure dark text on light background
        //    Tesseract expects black text on white background.
        //    If average intensity is low (<128), it's likely white text on dark bg, or mostly dark.
        //    An average intensity close to 0 is very dark, close to 255 is very light.
        // Check the top right pixel, if below threshold, invert the image
        if (!isInt){
            int topRight = currentImage.getRGB(currentImage.getWidth() - 21, 21) & 0xff;
            System.out.println("Top right pixel gray value: " + (topRight& 0xff));
            if (topRight < 120) {
                currentImage = invertGrayscaleImage(currentImage);
            }
        }
        else{
            double avgIntensity = getAveragePixelIntensity(currentImage);
            if (autoInvert && avgIntensity < 120) { // if image is mostly dark, invert it
                currentImage = invertGrayscaleImage(currentImage);
            }
            else if (!autoInvert){
                currentImage = invertGrayscaleImage(currentImage);
            }
        }


        // 4. Add padding
        if (paddingAmount > 0) {
            currentImage = addPadding(currentImage, paddingAmount, Color.WHITE); // Pad with white
        }


        // 5. Binarize using Otsu's method (generally robust)
        currentImage = otsuBinarize(currentImage);

        if (DEBUG){
            long tag = System.currentTimeMillis();
            FileUtil.writeImageToFile(currentImage, "preproc/preproc_" + tag + ".png");
        }
        return currentImage;
    }
}