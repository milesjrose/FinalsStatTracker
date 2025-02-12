package uk.ac.ed.inf;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import java.awt.Rectangle;

public class ScreenshotTaker {

    public static String captureScreenshot() {
        try {
            Robot robot = new Robot();
            java.awt.Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenshot = robot.createScreenCapture(screenRect);

            // Create a timestamped filename.
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "resources/screenshot_" + timestamp + ".png";
            File file = new File(fileName);
            // Ensure that the directory exists.
            file.getParentFile().mkdirs();

            ImageIO.write(screenshot, "png", file);
            return file.getAbsolutePath();
        } catch (AWTException | java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String cropImage(String imagePath, Rectangle rect) {
        try {
            File file = new File(imagePath);
            BufferedImage originalImage = ImageIO.read(file);

            // Validate crop dimensions
            if (rect.getX() < 0 || rect.getY() < 0 ||
                    rect.getX() + rect.getWidth() > originalImage.getWidth() ||
                    rect.getY() + rect.getHeight() > originalImage.getHeight()) {
                throw new IllegalArgumentException("Crop rectangle is out of bounds of the image.");
            }

            BufferedImage croppedImage = originalImage.getSubimage(
                    rect.x, rect.y, rect.width, rect.height);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            timestamp = timestamp + rect.x + rect.y;
            String croppedFileName = "resources/cropped_" + timestamp + ".png";
            File croppedFile = new File(croppedFileName);
            croppedFile.getParentFile().mkdirs();

            ImageIO.write(croppedImage, "png", croppedFile);
            return croppedFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String addRectangleToImage(String imagePath, Rectangle rect) {
        try {
            File file = new File(imagePath);
            BufferedImage image = ImageIO.read(file);

            // Draw the rectangle on the image.
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
            g2d.dispose();

            ImageIO.write(image, "png", file);
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
