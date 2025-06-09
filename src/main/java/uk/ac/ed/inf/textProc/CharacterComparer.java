package uk.ac.ed.inf.textProc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte; // For BufferedImage conversion
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import nu.pattern.OpenCV;
import uk.ac.ed.inf.model.BestMatch;

public class CharacterComparer {

    // Static block to load OpenCV native library using the org.openpnp loader
    static {
        try {
            OpenCV.loadLocally();
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV native library: " + e.getMessage());
            throw e;
        }
    }
    // Standard size for all characters before comparison
    private static final Size TEMPLATE_SIZE = new Size(32, 32);
    // Similarity threshold for a confident match (tune this value)
    private static final double SIMILARITY_THRESHOLD = 0.3; // Example: 0.0 to 1.0 for TM_CCOEFF_NORMED

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
    public BestMatch findBestMatch(BufferedImage unknownCharacterBI, Map<String, BufferedImage> templateBIs) {
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
        
        if (bestScore < SIMILARITY_THRESHOLD){
            return new BestMatch("com", 0.0);
        }
        return new BestMatch(bestMatchLabel, bestScore);
    }
}