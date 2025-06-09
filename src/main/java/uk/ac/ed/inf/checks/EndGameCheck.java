package uk.ac.ed.inf.checks;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ed.inf.model.Config;
import uk.ac.ed.inf.textProc.ImageProc;


public class EndGameCheck {
    private static final Logger logger = LoggerFactory.getLogger(EndGameCheck.class);

    public static boolean check (BufferedImage image){
        // If quick check, then detailed check
        return quickCheck(image) && detailedCheck(image);
    }

    public static boolean quickCheck(BufferedImage image){
        // Check if the luminence is over 0.7
        int luminance = 0;
        int threshold = 70;
        for (int i = 0; i < image.getWidth(); i++){
            for (int j = 0; j < image.getHeight(); j++){
                int rgb = image.getRGB(i, j);
                luminance += (int)(0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF) + 0.114 * (rgb & 0xFF));
            }
        }
        luminance = luminance / (image.getWidth() * image.getHeight());
        if (luminance > threshold){
            logger.info("Quick check passed, luminance: {} > {}", luminance, threshold);
            return true;
        }
        else {
            logger.info("Quick check failed, luminance: {} < {}", luminance, threshold);
            return false;
        }
    }

    public static boolean detailedCheck(BufferedImage image){
        // Check image proc for "summary" text.
        String summaryText = ImageProc.getSummaryText(ImageProc.crop(image, Config.summaryBox()));
        if (summaryText.contains("SUMMARY")){
            logger.info("Detailed check passed, summary text: {}", summaryText);
            return true;
        }
        else {
            logger.info("Detailed check failed, summary text: {}", summaryText);
            return false;
        }
    }
}