package uk.ac.ed.inf.textProc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte; // For BufferedImage conversion
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat; // If needed for other operations
import org.opencv.core.Size;    // If needed for other operations
import org.opencv.imgproc.Imgproc;

import nu.pattern.OpenCV;

public class CharacterComparer {

    // Static block to load OpenCV native library using the org.openpnp loader
    static {
        try {
            OpenCV.loadLocally();
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV native library: " + e.getMessage());
            e.printStackTrace();
            // Depending on the application, you might want to throw a runtime exception here
            // or have a more sophisticated error handling mechanism.
        }
    }
    // Standard size for all characters before comparison
    private static final Size TEMPLATE_SIZE = new Size(32, 32);
    // Similarity threshold for a confident match (tune this value)
    private static final double SIMILARITY_THRESHOLD = 0.65; // Example: 0.0 to 1.0 for TM_CCOEFF_NORMED

    /**
     * Converts a BufferedImage to a grayscale OpenCV Mat (CV_8UC1).
     * Handles common BufferedImage types.
     *
     * @param bi The BufferedImage to convert.
     * @return A grayscale OpenCV Mat, or an empty Mat if conversion fails or input is null.
     */
    private Mat bufferedImageToMat(BufferedImage bi) {
        if (bi == null) {
            System.err.println("Input BufferedImage is null for Mat conversion.");
            return new Mat(); // Return empty Mat
        }

        BufferedImage imageToProcess;

        // Ensure the image is in a format that can be easily converted to a grayscale Mat
        if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            imageToProcess = bi;
        } else {
            // Convert color or other types to grayscale BufferedImage first
            imageToProcess = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = imageToProcess.createGraphics();
            try {
                g.drawImage(bi, 0, 0, null);
            } finally {
                g.dispose();
            }
        }

        // Extract pixel data from the (now guaranteed) grayscale BufferedImage
        byte[] pixels = ((DataBufferByte) imageToProcess.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(imageToProcess.getHeight(), imageToProcess.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, pixels);
        return mat;
    }

    /**
     * Finds the best matching character from a set of template BufferedImages.
     *
     * @param unknownCharacterBI The BufferedImage of the unknown character.
     * @param templateBIs        A Map where the key is the character label (String)
     * and the value is the template BufferedImage.
     * @return The label of the best matching character, or null if no confident match is found.
     */
    public String findBestMatch(BufferedImage unknownCharacterBI, Map<String, BufferedImage> templateBIs) {
        if (unknownCharacterBI == null) {
            System.err.println("Unknown character BufferedImage is null.");
            return null;
        }
        if (templateBIs == null || templateBIs.isEmpty()) {
            System.err.println("Template character map is null or empty.");
            return null;
        }

        Mat unknownCharMatFull = bufferedImageToMat(unknownCharacterBI);
        if (unknownCharMatFull.empty()) {
            System.err.println("Failed to convert unknown character BufferedImage to Mat.");
            return null;
        }

        Mat unknownCharMatResized = new Mat();
        Imgproc.resize(unknownCharMatFull, unknownCharMatResized, TEMPLATE_SIZE);
        // Optional: If your segmenter doesn't produce perfectly binary images,
        // you might want to re-threshold here to ensure both unknown and template are binary.
        // e.g., Imgproc.threshold(unknownCharMatResized, unknownCharMatResized, 128, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);


        String bestMatchLabel = null;
        double bestScore = -Double.MAX_VALUE; // For TM_CCOEFF_NORMED, higher is better

        for (Map.Entry<String, BufferedImage> entry : templateBIs.entrySet()) {
            String charLabel = entry.getKey();
            BufferedImage templateBI = entry.getValue();

            if (templateBI == null) {
                System.err.println("Warning: Null template BufferedImage for character: " + charLabel);
                continue;
            }

            Mat templateMatFull = bufferedImageToMat(templateBI);
            if (templateMatFull.empty()) {
                System.err.println("Warning: Failed to convert template BufferedImage to Mat for character: " + charLabel);
                templateMatFull.release(); // Release even if empty
                continue;
            }

            Mat templateMatResized = new Mat();
            Imgproc.resize(templateMatFull, templateMatResized, TEMPLATE_SIZE);
            // Optional: Re-threshold templateMatResized if consistency is needed


            // Ensure Mats are of compatible types for matchTemplate (CV_8U or CV_32F)
            // Our bufferedImageToMat produces CV_8UC1, which is fine.
            if (unknownCharMatResized.type() != templateMatResized.type() ||
                unknownCharMatResized.channels() != templateMatResized.channels() ||
                templateMatResized.channels() != 1) { // We expect single channel
                System.err.println("Type or channel mismatch for template: " + charLabel +
                                   ". Unknown type: " + unknownCharMatResized.type() + " ch: " + unknownCharMatResized.channels() +
                                   ". Template type: " + templateMatResized.type() + " ch: " + templateMatResized.channels());
                templateMatFull.release();
                templateMatResized.release();
                continue;
            }


            Mat resultOutput = new Mat();
            // Using TM_CCOEFF_NORMED as it's robust to some lighting changes and gives a score between -1 and 1
            Imgproc.matchTemplate(unknownCharMatResized, templateMatResized, resultOutput, Imgproc.TM_CCOEFF_NORMED);

            Core.MinMaxLocResult mmr = Core.minMaxLoc(resultOutput);
            double score = mmr.maxVal; // For TM_CCOEFF_NORMED, maxVal is the similarity score

            if (score > bestScore) {
                bestScore = score;
                bestMatchLabel = charLabel;
            }

            // Release Mats created in the loop
            resultOutput.release();
            templateMatFull.release();
            templateMatResized.release();
        }

        // Release Mats created outside the loop
        unknownCharMatFull.release();
        unknownCharMatResized.release();

        if (bestMatchLabel != null && bestScore >= SIMILARITY_THRESHOLD) {
            // System.out.println("Confident match: " + bestMatchLabel + " with score: " + bestScore);
            return bestMatchLabel;
        } else {
             if (bestMatchLabel != null) {
                System.out.println("Match for '" + bestMatchLabel + "' below threshold. Score: " + bestScore);
             } else {
                System.out.println("No suitable match found.");
             }
            return null; // No confident match
        }
    }

    // --- Example Usage (for testing purposes) ---
    /*
    public static void main(String[] args) {
        // This main method is for illustrative testing.
        // You'd need to load your actual unknown character BufferedImage
        // and populate a Map<String, BufferedImage> with your templates.

        // 1. Create a dummy unknown character BufferedImage (e.g., a "7")
        BufferedImage unknownChar = new BufferedImage(30, 30, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gUnk = unknownChar.createGraphics();
        gUnk.setColor(java.awt.Color.WHITE); // Background
        gUnk.fillRect(0, 0, 30, 30);
        gUnk.setColor(java.awt.Color.BLACK); // Text
        gUnk.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        gUnk.drawString("7", 5, 25);
        gUnk.dispose();

        // 2. Create dummy template BufferedImages
        Map<String, BufferedImage> templates = new java.util.HashMap<>();

        BufferedImage template7 = new BufferedImage(30, 30, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g7 = template7.createGraphics();
        g7.setColor(java.awt.Color.WHITE);
        g7.fillRect(0, 0, 30, 30);
        g7.setColor(java.awt.Color.BLACK);
        g7.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        g7.drawString("7", 5, 25); // Similar '7'
        g7.dispose();
        templates.put("7", template7);

        BufferedImage templateA = new BufferedImage(30, 30, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gA = templateA.createGraphics();
        gA.setColor(java.awt.Color.WHITE);
        gA.fillRect(0, 0, 30, 30);
        gA.setColor(java.awt.Color.BLACK);
        gA.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        gA.drawString("A", 5, 25);
        gA.dispose();
        templates.put("A", templateA);


        // 3. Perform comparison
        CharacterComparer comparer = new CharacterComparer();
        String matchedChar = comparer.findBestMatch(unknownChar, templates);

        if (matchedChar != null) {
            System.out.println("Best match: " + matchedChar);
        } else {
            System.out.println("No confident match found.");
        }

        // Example of saving a BufferedImage (if you need to inspect them)
        // try {
        //     javax.imageio.ImageIO.write(unknownChar, "png", new java.io.File("unknown_char_test.png"));
        //     javax.imageio.ImageIO.write(template7, "png", new java.io.File("template_7_test.png"));
        // } catch (java.io.IOException e) {
        //     e.printStackTrace();
        // }
    }
    */
}