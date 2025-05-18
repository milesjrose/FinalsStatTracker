package uk.ac.ed.inf.checks;

import java.awt.image.BufferedImage;

import uk.ac.ed.inf.model.Regions;
import uk.ac.ed.inf.textProc.ImageProcessor;

public class EndGameCheck {
    public static boolean check (BufferedImage image){
        // If quick check, then detailed check
        return quickCheck(image) && detailedCheck(image);
    }

    public static boolean quickCheck(BufferedImage image){
        // Check if the luminence is over 0.7
        for (int i = 0; i < image.getWidth(); i++){
            for (int j = 0; j < image.getHeight(); j++){
                int rgb = image.getRGB(i, j);
                int luminance = (int)(0.299 * ((rgb >> 16) & 0xFF) + 0.587 * ((rgb >> 8) & 0xFF) + 0.114 * (rgb & 0xFF));
                if (luminance > 178){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean detailedCheck(BufferedImage image){
        // Check image proc for "summary" text.
        String summaryText = ImageProcessor.getText(image, Regions.getSummaryRegion());
        return summaryText.contains("Summary");
    }
}