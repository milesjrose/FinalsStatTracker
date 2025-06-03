package uk.ac.ed.inf.textProc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nu.pattern.OpenCV;

public class Preproc3 {

    private static final Logger logger = LoggerFactory.getLogger(Preproc3.class);   

    // Static block to load OpenCV native library using the org.openpnp loader
    static {
        try {
            OpenCV.loadLocally();
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV native library: " + e.getMessage());
            throw e;
        }
    }

    // --- Configuration for Contour Filtering ---
    // Minimum height of a character in pixels
    private static final int MIN_CHAR_HEIGHT = 8;
    // Maximum height of a character in pixels
    private static final int MAX_CHAR_HEIGHT = 50; // Adjust based on expected max character size
    // Minimum width of a character in pixels
    private static final int MIN_CHAR_WIDTH = 2;
    // Maximum width of a character in pixels
    private static final int MAX_CHAR_WIDTH = 50; // Adjust based on expected max character size
    // Minimum area of a character contour
    private static final double MIN_CONTOUR_AREA = 15; // e.g., a 3x5 character
    // Maximum area of a character contour
    private static final double MAX_CONTOUR_AREA = 1200; // e.g., a 30x40 character
    // Minimum aspect ratio (width / height)
    private static final double MIN_ASPECT_RATIO = 0.1;
    // Maximum aspect ratio (width / height)
    private static final double MAX_ASPECT_RATIO = 2.5; // A 'W' might be wide, an 'I' or '1' narrow


    /**
     * Segments characters from an input BufferedImage.
     * Assumes dark text on a light background.
     *
     * @param inputImage The BufferedImage containing text.
     * @return A List of BufferedImages, each representing an individual character (black text on white background).
     * Returns an empty list if no characters are found or if input is null.
     */
    public static List<BufferedImage> segmentCharacters(BufferedImage inputImage) {
        List<BufferedImage> characterImages = new ArrayList<>();
        if (inputImage == null) {
            System.err.println("Input image is null.");
            return characterImages;
        }

        // 1. Convert BufferedImage to OpenCV Mat
        Mat originalMat = bufferedImageToMat(inputImage);
        if (originalMat.empty()) {
            System.err.println("Could not convert BufferedImage to Mat.");
            return characterImages;
        }

        // 2. Preprocessing
        Mat grayMat = new Mat();
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY); // Assuming input is BGR

        Mat blurredMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurredMat, new Size(3, 3), 0); // Gentle blur to reduce noise

        Mat binaryMat = new Mat();
        Imgproc.threshold(blurredMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);


        // 3. Contour Detection
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        // RETR_EXTERNAL retrieves only the extreme outer contours.
        // CHAIN_APPROX_SIMPLE compresses horizontal, vertical, and diagonal segments and leaves only their end points.
        Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> McharacterBoundingBoxes = new ArrayList<>();

        // 4. Filter Contours
        for (MatOfPoint contour : contours) {
            Rect boundingBox = Imgproc.boundingRect(contour);
            double area = Imgproc.contourArea(contour);
            double aspectRatio = (double) boundingBox.width / boundingBox.height;

            // Apply filters
            if (boundingBox.height >= MIN_CHAR_HEIGHT && boundingBox.height <= MAX_CHAR_HEIGHT &&
                boundingBox.width >= MIN_CHAR_WIDTH && boundingBox.width <= MAX_CHAR_WIDTH &&
                area >= MIN_CONTOUR_AREA && area <= MAX_CONTOUR_AREA &&
                aspectRatio >= MIN_ASPECT_RATIO && aspectRatio <= MAX_ASPECT_RATIO) {
                McharacterBoundingBoxes.add(boundingBox);
            } else {
                logger.info("Discarded contour: Area=" + area + " H=" + boundingBox.height + " W=" + boundingBox.width + " AR=" + aspectRatio);
            }
        }

        if (McharacterBoundingBoxes.isEmpty()) {
            logger.info("No character contours found after filtering.");
            originalMat.release();
            grayMat.release();
            blurredMat.release();
            binaryMat.release();
            hierarchy.release();
            return characterImages;
        }

        // 5. Sort Contours (left-to-right)
        McharacterBoundingBoxes.sort(Comparator.comparingInt(rect -> rect.x));

        // 6. Crop characters and convert to BufferedImage
        for (Rect box : McharacterBoundingBoxes) {
            // Crop from the binary image
            Mat characterMat = new Mat(binaryMat, box);

            // Invert the character Mat to get black text on white background
            Mat invertedCharacterMat = new Mat();
            Core.bitwise_not(characterMat, invertedCharacterMat);

            // Add a small white border/padding                     -- not needed   
            int padding = 2; // 2 pixels padding
            Mat paddedCharMat = new Mat(invertedCharacterMat.rows() + 2 * padding,
                                        invertedCharacterMat.cols() + 2 * padding,
                                        invertedCharacterMat.type(),
                                        new Scalar(255));

            Rect roi = new Rect(padding, padding, invertedCharacterMat.cols(), invertedCharacterMat.rows());
            Mat submat = paddedCharMat.submat(roi);
            invertedCharacterMat.copyTo(submat);


            BufferedImage charImage = matToBufferedImage(paddedCharMat);
            if (charImage != null) {
                characterImages.add(charImage);
            }

            characterMat.release();
            invertedCharacterMat.release();
            paddedCharMat.release();
        }

        // Release OpenCV Mats
        originalMat.release();
        grayMat.release();
        blurredMat.release();
        binaryMat.release();
        hierarchy.release();

        return characterImages;
    }

    /**
     * Converts a BufferedImage to an OpenCV Mat.
     *
     * @param bi The BufferedImage to convert.
     * @return An OpenCV Mat object.
     */
    private static Mat bufferedImageToMat(BufferedImage bi) {
        byte[] data;
        int type;
        
        if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            type = CvType.CV_8UC1;
        } else {
            // Convert to 3-channel BGR
            BufferedImage converted = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            converted.getGraphics().drawImage(bi, 0, 0, null);
            data = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();
            type = CvType.CV_8UC3;
        }
        
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), type);
        mat.put(0, 0, data);
        return mat;
    }

    /**
     * Converts an OpenCV Mat to a BufferedImage.
     *
     * @param mat The Mat object.
     * @param bufferedImageType The type of BufferedImage to create (e.g., BufferedImage.TYPE_BYTE_GRAY).
     * @return A BufferedImage.
     */
    private static BufferedImage matToBufferedImage(Mat mat) {
        if (mat.empty()) {
            System.err.println("Cannot convert Mat to BufferedImage: Mat is empty.");
            return null;
        }

        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();
        
        byte[] data = new byte[width * height * channels];
        mat.get(0, 0, data);

        BufferedImage image = switch (channels) {
            case 1 -> {
                // Grayscale
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                byte[] grayData = new byte[width * height];
                System.arraycopy(data, 0, grayData, 0, grayData.length);
                img.getRaster().setDataElements(0, 0, width, height, grayData);
                yield img;
            }
            case 3 -> {
                // BGR to RGB conversion
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                img.getRaster().setDataElements(0, 0, width, height, data);
                yield img;
            }
            default -> {
                System.err.println("Unsupported number of channels: " + channels);
                yield null;
            }
        };

        return image;
    }
}
