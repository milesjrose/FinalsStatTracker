package uk.ac.ed.inf.textProc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import uk.ac.ed.inf.utility.FileWriterUtil;

public class Preproc2 {

    /**
     * Scales the image by a given factor.
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
        // For binary images, NEAREST_NEIGHBOR might be better if scaling after binarization,
        // but we scale before binarization, so BICUBIC is good for grayscale.
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return scaledImage;
    }

    /**
     * Converts an image to grayscale.
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
     */
    public static BufferedImage invertGrayscaleImage(BufferedImage grayscaleImage) {
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
        
        // For RescaleOp, a scale factor of -1.0 and an offset of 255 effectively inverts a grayscale image.
        // y = scaleFactor * x + offset
        // y = -1.0 * x + 255
        // If x = 0 (black), y = 255 (white)
        // If x = 255 (white), y = 0 (black)
        RescaleOp op = new RescaleOp(-1.0f, 255f, null);
        op.filter(nonIndexedImage, invertedImage);
        return invertedImage;
    }

    /**
     * Calculates the average pixel intensity of a grayscale image.
     * Returns a value between 0 (all black) and 255 (all white).
     */
    public static double getAveragePixelIntensity(BufferedImage grayscaleImage) {
        if (grayscaleImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            // System.err.println("Warning: getAveragePixelIntensity called on non-grayscale image. Result might be unexpected.");
            // Or convert it: grayscaleImage = toGrayscale(grayscaleImage);
        }
        long sumIntensity = 0;
        int width = grayscaleImage.getWidth();
        int height = grayscaleImage.getHeight();
        if (width == 0 || height == 0) return 128; // Avoid division by zero for empty image

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // For TYPE_BYTE_GRAY, getRGB returns the gray value in all R, G, B components.
                // We can take any of them, e.g., blue component.
                sumIntensity += (grayscaleImage.getRGB(x, y) & 0xff);
            }
        }
        return (double) sumIntensity / (width * height);
    }

    /**
     * Binarizes a grayscale image using a fixed threshold.
     * Pixels darker than threshold become black, others become white.
     */
    public static BufferedImage binarize(BufferedImage grayscaleImage, int threshold) {
        if (grayscaleImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            System.err.println("Warning: binarize called on non-grayscale image. Converting to gray first.");
            grayscaleImage = toGrayscale(grayscaleImage);
        }
        BufferedImage binaryImage = new BufferedImage(grayscaleImage.getWidth(), grayscaleImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < grayscaleImage.getHeight(); y++) {
            for (int x = 0; x < grayscaleImage.getWidth(); x++) { // Corrected: grayscaleImage.getWidth()
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
     */
    public static BufferedImage otsuBinarize(BufferedImage grayscaleImage) {
        if (grayscaleImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            System.err.println("Warning: otsuBinarize called on non-grayscale image. Converting to gray first.");
            grayscaleImage = toGrayscale(grayscaleImage);
        }
        int threshold = getOtsuThreshold(grayscaleImage);
        return binarize(grayscaleImage, threshold);
    }

    /**
     * Calculates the optimal threshold for binarization using Otsu's method.
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
        int wF = 0; // Weight Foreground

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
        return threshold + 1; // +1 can sometimes give slightly better visual results
    }


    /**
     * Adds padding around the image.
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
     */
    public static BufferedImage preprocessForOCR(BufferedImage originalImage, double scaleFactor, int paddingAmount) {
        BufferedImage currentImage = originalImage;

        // 1. Add padding (especially if the original crop is tight)
        if (paddingAmount > 0) {
            currentImage = addPadding(currentImage, paddingAmount, Color.BLACK); // Pad with black
        }

        FileWriterUtil.writeImageToFile(currentImage, "debug2_1");

        // 2. Scale the image (crucial for small characters)
        if (scaleFactor > 1.0) {
            currentImage = scaleImage(currentImage, scaleFactor);
        }

        FileWriterUtil.writeImageToFile(currentImage, "debug2_2");

        // 3. Convert to Grayscale
        currentImage = toGrayscale(currentImage);

        FileWriterUtil.writeImageToFile(currentImage, "debug2_3");

        currentImage = binarize(currentImage, 128);

        FileWriterUtil.writeImageToFile(currentImage, "debug2_3a");

        // 4. Ensure dark text on light background
        //    Tesseract expects black text on white background.
        //    If average intensity is low (<128), it's likely white text on dark bg, or mostly dark.
        //    An average intensity close to 0 is very dark, close to 255 is very light.
        double avgIntensity = getAveragePixelIntensity(currentImage);
        if (avgIntensity < 120) { // Heuristic: if image is mostly dark, invert it
                                  // This assumes text is lighter than background in this case
            currentImage = invertGrayscaleImage(currentImage);
        }

        FileWriterUtil.writeImageToFile(currentImage, "debug2_4");

        // After this step, we assume text is darker than background.

        // 5. Binarize using Otsu's method (generally robust)
        currentImage = otsuBinarize(currentImage);

        // 6. Final check & potential inversion for binary image:
        //    Ensure text is black. If white pixels are fewer than black pixels,
        //    it means we have white text on black background. So invert.
        //    (This step might be redundant if step 4 worked perfectly, but it's a good safeguard)
        //int whitePixels = 0;
        //int blackPixels = 0;
        //for (int y = 0; y < currentImage.getHeight(); y++) {
        //    for (int x = 0; x < currentImage.getWidth(); x++) {
        //        if ((currentImage.getRGB(x, y) & 0xff) == 255) { // In TYPE_BYTE_BINARY, white is 255
        //            whitePixels++;
        //        } else {
        //            blackPixels++;
        //        }
        //    }
        //}
        // If black pixels are dominant, we have black text on white.
        // If white pixels are dominant, we have white text on black background (invert).
        // This also handles cases where Otsu might invert based on histogram distribution.
        //if (whitePixels < blackPixels) { // More black pixels than white implies text might be white
            // This means the dominant color is black (background), so text is white. Invert.
            //currentImage = invertGrayscaleImage(currentImage); // invertImage works on binary too
        //}


        return currentImage;
    }
}