package uk.ac.ed.inf;
// Main.java
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements NativeKeyListener {
    static Regions regions;


    public static void main(String[] args) {
        // Disable excessive logging from JNativeHook
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        regions = new Regions(new Rectangle(700, 290, 260, 50),
                new Rectangle(625, 960, 415, 290),
                new Rectangle(1150, 290, 260, 50),
                new Rectangle(1080, 960, 415, 290),
                new Rectangle(1600, 290, 260, 50),
                new Rectangle(1530, 960, 415, 290));

        GlobalScreen.addNativeKeyListener(new Main());
        System.out.println("Press F9 to capture screenshot and extract stats.");
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Listen for F9 press
        if (e.getKeyCode() == NativeKeyEvent.VC_F9) {
            System.out.println("F9 pressed! Capturing screenshot...");
            // Capture screenshot
            String screenshotPath = ScreenshotTaker.captureScreenshot();
            System.out.println("Screenshot saved to: " + screenshotPath);

            ArrayList<String> stats = new ArrayList<>();
            // Process screenshot to extract stats via OCR
            for (Rectangle region : regions.getRegions()){
                String stat = OCRProcessor.extractTextFromImage(screenshotPath,region);
                stats.add(stat);
                ScreenshotTaker.addRectangleToImage(screenshotPath, region);
                System.out.println("Extracted stats: ");
                System.out.println(stat);
            }

            // Save stats to a text file
            FileWriterUtil.writeStatsToFile(stats.toString());
            System.out.println("Stats saved to file.");
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) { }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) { }
}
