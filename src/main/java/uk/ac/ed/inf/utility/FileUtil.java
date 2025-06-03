package uk.ac.ed.inf.utility;

// FileWriterUtil.java
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    public static void writeStatsToFile(String stats) {
        logger.info("Writing stats to file");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "stats_" + timestamp + ".txt";

        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("Timestamp: " + timestamp + "\n");
            writer.write("Stats:\n");
            writer.write(stats);
            writer.write("\n\n");
        } catch (IOException e) {
            System.out.println("Error writing stats to file: " + e.getMessage());
        }
    }

    public static void writeImageToFile(BufferedImage image, String fileName){
        String imageName = "resources/" + fileName + ".png";
        try {
            ImageIO.write(image, "png", new File(imageName));
        } catch (IOException e) {
            System.out.println("Error writing image to file: " + e.getMessage());
        }
    }

    public static void writeDebugImage(BufferedImage image, String fileName) {
        try {
            File file = new File(fileName);
            // Create parent directories if they don't exist
            file.getParentFile().mkdirs();
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            System.out.println("Error writing debug image to file: " + e.getMessage());
        }
    }

    public static Map<String, BufferedImage> loadTemplates(){
        Map<String, BufferedImage> templates = new java.util.HashMap<>();
        // Load templates from resources/templates. Each template is a file with a single char, named after the char.png
        File templatesDir = new File("resources/templates");
        for (File template : templatesDir.listFiles()){
            BufferedImage templateImage = Screenshot.fullLoadImage(template.getAbsolutePath());
            templates.put(template.getName().split("\\.")[0], templateImage);
        }
        return templates;
    }
}
