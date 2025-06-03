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

    // Get text from image
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

    // Get int from image (splits into chars to avoid OCR errors)
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

    // Compare unknown character to templates
    public static String compareChars(BufferedImage unknownChar){
        // Load templates from resources/templates. Each template is a file with a single char, named after the char.png
        Map<String, BufferedImage> templates = FileUtil.loadTemplates();

        // Compare unknown character to templates
        CharacterComparer comparer = new CharacterComparer();
        String bestMatch = comparer.findBestMatch(unknownChar, templates);
        System.out.println("Best match: " + bestMatch);
        return bestMatch;
    }

    // Preprocess image
    private static BufferedImage preprocess(BufferedImage image){
        // Preprocess image
        image = Preproc.preprocessForOCR(image);
        return image;
    }

    // Preprocess image for OCR
    private static BufferedImage preprocessForOCR(BufferedImage image){
        // Preprocess image
        image = Preproc2.preprocessForOCR(image, 2, 10);
        return image;
    }

    // Segment chars
    private static List<BufferedImage> segmentChars(BufferedImage image){
        // Segment chars
        return Preproc3.segmentCharacters(image);
    }

    // Crop image
    public static BufferedImage crop(BufferedImage image, Rectangle region){
        return image.getSubimage(region.x, region.y, region.width, region.height);
    }
    
}
