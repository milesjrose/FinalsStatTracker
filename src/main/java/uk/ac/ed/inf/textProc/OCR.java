package uk.ac.ed.inf.textProc;

// OCRProcessor.java
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import uk.ac.ed.inf.utility.FileUtil;

public class OCR {
    private static final Logger logger = LoggerFactory.getLogger(OCR.class);
    private static final boolean DEBUG = false;

    /**
     * Gets a Tesseract instance
     * 
     * @param isText Boolean of whether to get a text or number Tesseract instance
     * @return Tesseract instance
     */
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
            tesseract.setTessVariable("textord_min_linesize", "1.5"); // Increase minimum line size
            tesseract.setTessVariable("textord_heavy_nr", "1"); // More sensitive to vertical lines
            tesseract.setTessVariable("textord_min_linesize", "1.5"); // Lower minimum line size to catch thinner "1"s
        }
        tesseract.setOcrEngineMode(3);
        tesseract.setPageSegMode(isText ? 6 : 7);    // Use different modes for text vs numbers
        return tesseract;
    }

    /**
     * OCR processing
     * 
     * @param image BufferedImage of image
     * @param isText Boolean of whether to get a text or number Tesseract instance
     * @return String of OCR result
     */
    private static String proccess(BufferedImage image, Boolean isText) {
        Tesseract tesseract = getTesseract(isText);
        BufferedImage imageToProcess = image;

        if (DEBUG) {
            // Rectangle image
            // FileWriterUtil.writeImageToFile(Screenshot.addRectangleToImage(image, region), "resources/debug.png");
            // Preprocessed image   
            FileUtil.writeDebugImage(imageToProcess, "resources/ocr/debug_" + System.currentTimeMillis() + ".png");
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

    /**
     * Gets text from an image
     * 
     * @param image BufferedImage of image
     * @return String of text from image
     */
    public static String getText(BufferedImage image){
        return proccess(image, true);
    }

    /**
     * Gets integers from an image
     * 
     * @param image BufferedImage of image
     * @return String of integers from image
     */
    public static String getInt(BufferedImage image){
        return proccess(image, false);
    }
}