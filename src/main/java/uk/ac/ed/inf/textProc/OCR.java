package uk.ac.ed.inf.textProc;

// OCRProcessor.java
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import uk.ac.ed.inf.utility.FileWriterUtil;

public class OCR {
    private static final Logger logger = LoggerFactory.getLogger(OCR.class);
    private static final boolean DEBUG = true;

    public static String getText(BufferedImage image, Rectangle region){
        return getText(image, region, true, DEBUG);
    }

    public static String[] getText(BufferedImage image, Rectangle[] regions){
        String[] results = new String[regions.length];
        for (int i = 0; i < regions.length; i++){
            results[i] = getText(image, regions[i], true, false).strip();
        }
        return results;
    }


    public static int getInt(BufferedImage image, Rectangle region){
        return Integer.parseInt(getText(image, region, false, DEBUG).strip());
    }

    public static int[] getInt(BufferedImage image, Rectangle[] regions){
        int[] results = new int[regions.length];
        for (int i = 0; i < regions.length; i++){
            try {
                results[i] = Integer.parseInt(getText(image, regions[i], false, false).strip());
            } catch (NumberFormatException e) {
                results[i] = 0;
            }
        }
        return results;
    }

    private static Tesseract getTesseract(Boolean isText){
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("eng");
        tesseract.setTessVariable("user_defined_dpi", "300");
        if (isText){
            tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,"); // Only recognize numbers & letters
        } else {
            tesseract.setTessVariable("tessedit_char_whitelist", "0123456789"); // Only recognize numbers
            tesseract.setTessVariable("tessedit_pageseg_mode", "6"); // Treat as a single line of text
            tesseract.setTessVariable("textord_min_linesize", "2.5"); // Increase minimum line size
            tesseract.setTessVariable("textord_heavy_nr", "1"); // More sensitive to vertical lines
            tesseract.setTessVariable("textord_min_linesize", "1.5"); // Lower minimum line size to catch thinner "1"s
        }
        tesseract.setOcrEngineMode(3);
        tesseract.setPageSegMode(isText ? 6 : 7);    // Use different modes for text vs numbers
        return tesseract;
    }

    public static String getText(BufferedImage image, Rectangle region, Boolean isText, Boolean debug) {
        Tesseract tesseract = getTesseract(isText);

        BufferedImage imageToProcess = image;

        if (region != null) {
            // Important: Crop *before* padding and scaling if region is for original image
            imageToProcess = imageToProcess.getSubimage(region.x, region.y, region.width, region.height);
        }

        // Apply the improved preprocessing:
        double scale = 3.0; // Experiment with this
        int padding = 10;   // Experiment with this
        imageToProcess = Preproc.preprocessForOCR(imageToProcess);


        if (DEBUG) {
            // Rectangle image
            //FileWriterUtil.writeImageToFile(Screenshot.addRectangleToImage(image, region), "resources/debug.png");
            // Preprocessed image   
            FileWriterUtil.writeDebugImage(imageToProcess, "resources/mono/debug_" + System.currentTimeMillis() + ".png");
        }

        try {
            // Perform OCR on the preprocessed image
            String result = tesseract.doOCR(imageToProcess);
            //logger.info("OCR Result: {}", result);
            return result.strip().replace("\n", "");
        } catch (TesseractException e) {
            logger.error("OCR Failed, {}", e.getMessage());
            return "OCR Failed";
        }
    }
}