package uk.ac.ed.inf.utility;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Screenshot {

    private static final Logger logger = LoggerFactory.getLogger(Screenshot.class);

    public static String saveScreenshot() {
        try {
            BufferedImage screenshot = getScreenshot();
            if (screenshot == null){
                return null;
            }

            // Create a timestamped filename.
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "resources/screenshot_" + timestamp + ".png";
            File file = new File(fileName);
            // Ensure that the directory exists.
            file.getParentFile().mkdirs();

            ImageIO.write(screenshot, "png", file);
            return file.getAbsolutePath();
        } catch (java.io.IOException e) {
            System.out.println("Error saving screenshot: " + e.getMessage());
            return null;
        }
    }

    public static BufferedImage getScreenshot() {
        try {
            Robot robot = new Robot();
            java.awt.Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenshot = robot.createScreenCapture(screenRect);
            
             // print screenshot size
            System.out.println("Screenshot size: " + screenshot.getWidth() + "x" + screenshot.getHeight());

            return screenshot;
        } catch (AWTException e) {
            System.out.println("Error getting screenshot: " + e.getMessage());
            return null;
        }
    }

    public static BufferedImage cropImage(BufferedImage originalImage, Rectangle rect) {
        try {
            // Validate crop dimensions
            if (rect.getX() < 0 || rect.getY() < 0 ||
                    rect.getX() + rect.getWidth() > originalImage.getWidth() ||
                    rect.getY() + rect.getHeight() > originalImage.getHeight()) {
                throw new IllegalArgumentException("Crop rectangle is out of bounds of the image.");
            }
            
            BufferedImage croppedImage = originalImage.getSubimage(
                    rect.x, rect.y, rect.width, rect.height);

            return croppedImage;
        } catch (IllegalArgumentException e) {
            System.out.println("Error cropping image: " + e.getMessage());
            return null;
        }
    }

    public static BufferedImage addRectangleToImage(BufferedImage image, Rectangle rect) {
        try {
            // Draw the rectangle on the image.
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
            g2d.dispose();
            return image;
        } catch (Exception e) {
            System.out.println("Error adding rectangle to image: " + e.getMessage());
            return null;
        }
    }

    public static BufferedImage addRectanglesToImage(BufferedImage image, Rectangle[] rects) {
        BufferedImage debugImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2d = debugImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        for (Rectangle rect : rects){
            debugImage = addRectangleToImage(debugImage, rect);
        }
        return debugImage;
    }

    public static void clearFiles(){
        //clear screenshot files
        File directory = new File("resources/");
        String[] fileNames = {"screenshot", "monochrome", "rectangle", "debug"};
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                for (String fileName : fileNames){
                    if (file.getName().contains(fileName)){
                        file.delete();
                    }
                }
            }
        }
        // clear other folders
        for (String folder : new String[]{"mono", "ocr"}){
            clearFolder(folder);
        }
    }

    private static void clearFolder(String folder){
        String[] fileNames = {"screenshot", "monochrome", "rectangle", "debug"};
        File directory = new File("resources/" + folder + "/");
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                for (String fileName : fileNames){
                    if (file.getName().contains(fileName)){
                        file.delete();
                    }
                }
            }
        }
    }

    public static BufferedImage loadImage(String name){
        File directory = new File("resources/");
        File[] files = directory.listFiles();
        for (File file : files){
            if (file.getName().contains(name)){
                return fullLoadImage(file.getAbsolutePath());
            }
        }
        logger.error("Image not found: " + name);
        return null;
    }

    public static BufferedImage fullLoadImage(String filepath){
        logger.debug("Loading image from: " + filepath);
        try{
            BufferedImage image = ImageIO.read(new File(filepath));
            return image;
        } catch (IOException e){
            System.out.println("Error loading image: " + e.getMessage());
            return null;
        }
    }
}
