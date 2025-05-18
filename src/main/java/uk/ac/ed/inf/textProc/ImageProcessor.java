package uk.ac.ed.inf.textProc;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import uk.ac.ed.inf.model.Point;

public class ImageProcessor {
    // Get text from image
    public static String getText(BufferedImage image, Rectangle region){
        return OCRProcessor.extractTextFromImage(image, region);
    }
    public static String getText(String imagePath, Rectangle region){
        try {        
            File imageFile = new File(imagePath);
            BufferedImage fullImage = ImageIO.read(imageFile);
            return OCRProcessor.extractTextFromImage(fullImage, region);
        } catch (java.io.IOException e) {
            System.out.println("Error getting text: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    // Check end game pixels
    public static boolean checkEndGame(BufferedImage image, List<Point> points){
        // Check if the pixels at given points are white
        for (Point p : points){
            if (image.getRGB(p.x, p.y) != Color.WHITE.getRGB()){
                return false;
            }
        }
        return true;
    }
    
}
