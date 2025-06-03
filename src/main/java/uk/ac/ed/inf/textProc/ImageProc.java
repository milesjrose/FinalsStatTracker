package uk.ac.ed.inf.textProc;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ed.inf.utility.FileUtil;


public class ImageProc {
    private static final Logger logger = LoggerFactory.getLogger(ImageProc.class);

    /**
     * Extracts text from an image
     * 
     * @param image BufferedImage of text
     * @return Text from image
     */
    public static String getText(BufferedImage image){
        // Preprocess image
        image = preprocess(image);
        // Return text
        try {
            return OCR.getText(image);
        } catch (Exception e) {
            logger.error("Error getting text from image", e);
            return null;
        }
    }

    /**
     * Extracts integers from an image
     * Splits into chars to reduce OCR errors, and uses comparison to templates as backup.
     *
     * @param image BufferedImage of integers
     * @return Integer from image
     */
    public static int getInt(BufferedImage image){
        // Preprocess image
        image = preprocess(image);

        // Split chars
        List<BufferedImage> chrs = segmentChars(image);
        if (chrs.isEmpty()){
            System.out.println("No characters found");
            return 0;
        }

        // Get strings
        String result = "";
        for (BufferedImage chr : chrs){
            // Preprocess image
            BufferedImage preChr = preprocessForOCR(chr);
            String res = OCR.getInt(preChr);
            if (res.isEmpty()){
                String bestMatch = compareChars(preChr);
                if (bestMatch.equals("com")){
                    continue;
                } else {
                    result += bestMatch;
                }
            }
            result += res;
        }

        // Convert to int and return
        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e){
            logger.error("Error parsing int: " + result);
            return 0;
        }
    }

    /**
     * Compares an unknown character to templates
     * 
     * @param unknownChar BufferedImage of unknown character
     * @return Best match from templates
     */
    public static String compareChars(BufferedImage unknownChar){
        // Load templates from resources/templates. Each template is a file with a single char, named after the char.png
        Map<String, BufferedImage> templates = FileUtil.loadTemplates();

        // Compare unknown character to templates
        CharacterComparer comparer = new CharacterComparer();
        String bestMatch = comparer.findBestMatch(unknownChar, templates);
        System.out.println("Best match: " + bestMatch);
        return bestMatch;
    }

    /**
     * Preprocesses an image
     * 
     * @param image BufferedImage of image
     * @return Preprocessed image
     */
    private static BufferedImage preprocess(BufferedImage image){
        // Preprocess image
        image = Preproc.preprocessForOCR(image);
        return image;
    }

    /**
     * Preprocesses an image for OCR
     * 
     * @param image BufferedImage of image
     * @return Preprocessed image
     */
    private static BufferedImage preprocessForOCR(BufferedImage image){
        // Preprocess image
        image = Preproc2.preprocessForOCR(image, 2, 10);
        return image;
    }

    /**
     * Segments characters from an image
     * 
     * @param image BufferedImage of image
     * @return List of BufferedImages of characters
     */
    private static List<BufferedImage> segmentChars(BufferedImage image){
        // Segment chars
        return Preproc3.segmentCharacters(image);
    }

    /**
     * Crops an image
     * 
     * @param image BufferedImage of image
     * @param region Rectangle of region to crop
     * @return Cropped image
     */
    public static BufferedImage crop(BufferedImage image, Rectangle region){
        return image.getSubimage(region.x, region.y, region.width, region.height);
    }
    
}
