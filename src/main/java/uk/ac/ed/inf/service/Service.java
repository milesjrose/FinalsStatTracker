package uk.ac.ed.inf.service;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ed.inf.model.PlayerStats;

public class Service {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);
    private static volatile boolean isAutoProcessing = false;
    private static Thread autoProcessingThread;

    public static void procEndGame(BufferedImage screenshot){
        logger.info("Processing end game");
        if (CollectionService.isEndGame(screenshot)){
            logger.info("End game detected");
            for (int player = 1; player <= 3; player++){
                PlayerStats stats = CollectionService.parse(screenshot, player);
                DBService.save(stats);
            }
            logger.info("End game processed");
        }
        else{
            logger.info("No end game detected");
        }
    }

    public static void startAutoProcessing() {
        if (isAutoProcessing) {
            logger.warn("Auto processing is already running.");
            return;
        }
        isAutoProcessing = true;
        autoProcessingThread = new Thread(() -> {
            logger.info("Auto processing started");
            while (isAutoProcessing) {
                procEndGame(CollectionService.screenshot());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (!isAutoProcessing) {
                        Thread.currentThread().interrupt(); // Restore interrupt status
                        break; // Exit if interrupted while stopping
                    }
                    e.printStackTrace();
                }
            }
            logger.info("Auto processing stopped");
        });
        autoProcessingThread.start();
    }

    public static void stopAutoProcessing() {
        isAutoProcessing = false;
        if (autoProcessingThread != null) {
            autoProcessingThread.interrupt(); // Interrupt the thread to wake it from sleep
        }
    }
}
