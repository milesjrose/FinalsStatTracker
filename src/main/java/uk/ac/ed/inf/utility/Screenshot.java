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

public class Screenshot {

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

    public static void clearFiles(){
        File directory = new File("resources/");
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains("screenshot")){
                    file.delete();
                }
            }
        }
    }

    public static BufferedImage loadImage(String filepath){
        try{
            BufferedImage image = ImageIO.read(new File(filepath));
            return image;
        } catch (IOException e){
            System.out.println("Error loading image: " + e.getMessage());
            return null;
        }
    }
}
