package uk.ac.ed.inf.textProc;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ed.inf.utility.FileWriterUtil;


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
        int i = new Random().nextInt(1000);
        String result = "";
        for (BufferedImage chr : chrs){
            // Preprocess image
            BufferedImage preChr = preprocessForOCR(chr);
            String res = OCR.getInt(preChr);
            try {
                Integer.parseInt(res);
                result += res;
            } catch (NumberFormatException e){
                FileWriterUtil.writeImageToFile(preChr, "debug_char_" + i);
                logger.error("Error parsing int " + i + ": " + res);
            }
            i++;
        }

        // Convert to int and return
        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e){
            logger.error("Error parsing int: " + result);
            return 0;
        }
    }

    private static BufferedImage preprocess(BufferedImage image){
        // Preprocess image
        image = Preproc.preprocessForOCR(image);
        return image;
    }

    private static BufferedImage preprocessForOCR(BufferedImage image){
        // Preprocess image
        image = Preproc2.preprocessForOCR(image, 2, 10);
        return image;
    }

    private static List<BufferedImage> segmentChars(BufferedImage image){
        // Segment chars
        return Preproc3.segmentCharacters(image);
    }

    // Crop image
    public static BufferedImage crop(BufferedImage image, Rectangle region){
        return image.getSubimage(region.x, region.y, region.width, region.height);
    }
    
}
